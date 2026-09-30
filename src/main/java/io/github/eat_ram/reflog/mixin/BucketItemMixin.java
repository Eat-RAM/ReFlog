package io.github.eat_ram.reflog.mixin;

import io.github.eat_ram.reflog.block.CustomFluidloggable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {
    @Shadow
    @Final
    protected Fluid fluid;

    @Shadow
    protected abstract void playEmptyingSound(
        @Nullable LivingEntity user, WorldAccess world, BlockPos pos
    );

    @Unique
    private static String reflog$encode(
        Block block, StringProperty ppt, FluidState fluidState
    ) {
        return FluidStateTranscript.escape(
            block instanceof Waterloggable &&
            ppt.getName().equals(WATERLOGGED.getName()) ?
            FluidStateTranscript.transcriptWaterlogged(fluidState) :
            FluidStateTranscript.transcript(fluidState)
        );
    }

    @ModifyVariable(method = "use", at = @At("STORE"), ordinal = 2)
    private BlockPos modifyPlacePos(
        BlockPos placePos, World level, PlayerEntity player, Hand hand,
        @Local(ordinal = 0) BlockPos pos,
        @Local(ordinal = 0) BlockState clicked
    ) {
        Block block = clicked.getBlock();
        if (block instanceof CustomFluidloggable ||
            ModifyExisting.OVERRIDES.containsKey(block.getClass())) {
            return pos;
        }
        return placePos;
    }

    @Inject(method = "use", at = @At(
        value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
    ), cancellable = true)
    private void onDrainOverrideBlock(
        World level, PlayerEntity player, Hand hand,
        CallbackInfoReturnable<ActionResult> cir,
        @Local(ordinal = 0) ItemStack itemStack,
        @Local(ordinal = 0) BlockPos pos
    ) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        StringProperty ppt = ModifyExisting.OVERRIDES.get(block.getClass());
        if (ppt != null) {
            String raw = FluidStateTranscript.unescape(state.get(ppt));
            FluidState fstate = block instanceof Waterloggable &&
                ppt.getName().equals(WATERLOGGED.getName())
                ? FluidStateTranscript.restoreWaterlogged(raw, true)
                : FluidStateTranscript.restore(raw, true);
            if (fstate.isStill()) {
                Item bucketItem = fstate.getFluid().getBucketItem();
                if (bucketItem != null) {
                    BlockState newState = state.with(ppt, ppt.getValues().get(0));
                    level.setBlockState(pos, newState, Block.NOTIFY_ALL_AND_REDRAW);
                    if (!newState.canPlaceAt(level, pos)) {
                        level.breakBlock(pos, true);
                    }
                    ItemStack taken = new ItemStack(bucketItem);
                    player.incrementStat(Stats.USED.getOrCreateStat((BucketItem)(Object)this));
                    fstate.getFluid().getBucketFillSound().ifPresent(
                        sound -> player.playSound(sound, 1f, 1f)
                    );
                    level.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
                    ItemStack result = ItemUsage.exchangeStack(itemStack, player, taken);
                    if (!level.isClient()) {
                        Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity)player, taken);
                    }
                    cir.setReturnValue(ActionResult.SUCCESS.withNewHandStack(result));
                }
            }
        }
    }

    @ModifyVariable(method = "placeFluid", at = @At("STORE"), ordinal = 2)
    private boolean modifyPlaceLiquid(
        boolean placeLiquid, @Nullable LivingEntity user, World world,
        BlockPos pos, @Nullable BlockHitResult hitResult,
        @Local(ordinal = 0) BlockState blockState,
        @Local(ordinal = 0) Block block
    ) {
        StringProperty ppt = ModifyExisting.OVERRIDES.get(block.getClass());
        if (ppt != null && this.fluid instanceof FlowableFluid flowable) {
            FluidState target = flowable.getStill(false);
            String escaped = reflog$encode(block, ppt, target);
            return ppt.getValues().contains(escaped) &&
                   !blockState.get(ppt).equals(escaped);
        }
        return placeLiquid;
    }

//    @WrapOperation(method = "placeFluid", at = @At(
//        value = "INVOKE",
//        target = "Lnet/minecraft/block/FluidFillable;tryFillWithFluid(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Z"
//    ))
//    private boolean wrapTryFillWithFluid(
//        FluidFillable instance, WorldAccess world, BlockPos pos,
//        BlockState blockState, FluidState fluidState,
//        Operation<Boolean> original
//    ) {
//        Block block = blockState.getBlock();
//        StringProperty ppt = ModifyExisting.OVERRIDES.get(block.getClass());
//        if (ppt != null) {
//            String escaped = reflog$encode(block, ppt, fluidState);
//            if (ppt.getValues().contains(escaped) &&
//                !blockState.get(ppt).equals(escaped)) {
//                if (!world.isClient()) {
//                    world.setBlockState(
//                        pos, blockState.with(ppt, escaped),
//                        Block.NOTIFY_ALL_AND_REDRAW
//                    );
//                    world.scheduleFluidTick(
//                        pos, fluidState.getFluid(),
//                        fluidState.getFluid().getTickRate(world)
//                    );
//                }
//                return true;
//            }
//            return false;
//        }
//        return original.call(instance, world, pos, blockState, fluidState);
//    }

    @Inject(method = "placeFluid", at = @At(
        value = "INVOKE", target = "Lnet/minecraft/world/World;isClient()Z"
    ), cancellable = true)
    private void onPlaceIntoNonWaterFluidFillable(
        @Nullable LivingEntity user, World world, BlockPos pos,
        @Nullable BlockHitResult hitResult,
        CallbackInfoReturnable<Boolean> cir,
        @Local(ordinal = 0) FlowableFluid flowingFluid,
        @Local(ordinal = 0) BlockState blockState,
        @Local(ordinal = 0) Block block
    ) {
        FluidState target = flowingFluid.getStill(false);
        StringProperty ppt = ModifyExisting.OVERRIDES.get(block.getClass());
        if (ppt != null) {
            String escaped = reflog$encode(block, ppt, target);
            if (ppt.getValues().contains(escaped)) {
                if (!world.isClient()) {
                    world.setBlockState(
                        pos, blockState.with(ppt, escaped),
                        Block.NOTIFY_ALL_AND_REDRAW
                    );
                    world.scheduleFluidTick(
                        pos, target.getFluid(),
                        target.getFluid().getTickRate(world)
                    );
                }
                this.playEmptyingSound(user, world, pos);
                cir.setReturnValue(true);
            }
            return;
        }
        if (block instanceof FluidFillable container) {
            container.tryFillWithFluid(world, pos, blockState, target);
            this.playEmptyingSound(user, world, pos);
            cir.setReturnValue(true);
        }
    }
}
