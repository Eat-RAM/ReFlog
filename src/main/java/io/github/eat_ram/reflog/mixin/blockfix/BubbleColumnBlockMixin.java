package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.BubbleColumnBlock.class)
public abstract class BubbleColumnBlockMixin {
    @WrapWithCondition(method = "getStateForNeighborUpdate", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/world/tick/ScheduledTickView;scheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;I)V"
    ))
    private boolean tickFluid(
        ScheduledTickView instance, BlockPos pos, Fluid fluid, int delay,
        final BlockState state, final WorldView world,
        final ScheduledTickView tickView, final BlockPos pos2,
        final Direction direction, final BlockPos neighborPos,
        final BlockState neighborState, final Random random
    ) {
        StringProperty fluidlogged =
        ModifyExisting.CACHED_PPTS.get(state.getBlock());
        if (fluidlogged != null) {
            return false;
        }
        return true;
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At(
        value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER,
        target = "Lnet/minecraft/world/tick/ScheduledTickView;scheduleFluidTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/fluid/Fluid;I)V"
    ))
    private void tickFluid(
        final BlockState state, final WorldView world,
        final ScheduledTickView tickView, final BlockPos pos,
        final Direction direction, final BlockPos neighborPos,
        final BlockState neighborState, final Random random,
        CallbackInfoReturnable<BlockState> cir
    ) {
        StringProperty fluidlogged =
        ModifyExisting.CACHED_PPTS.get(state.getBlock());
        if (fluidlogged != null) {
            Fluid fluid = (FluidStateTranscript.restore(
                FluidStateTranscript.unescape(state.get(fluidlogged)), true
            )).getFluid();
            tickView.scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
        }
    }

    @WrapOperation(method = "getBubbleState", at = @At(
        value = "INVOKE", ordinal = 2,
        target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"
    ))
    private static BlockState modifyBubbleState(
        Block instance, Operation<BlockState> original,
        final Block bubbleColumn, final BlockState state,
        final BlockState occupyState
    ) {
        StringProperty fluidlogged =
        ModifyExisting.CACHED_PPTS.get(occupyState.getBlock());
        if (fluidlogged != null) {
            return occupyState.getFluidState().getBlockState();
        }
        return original.call(instance);
    }

    @ModifyReturnValue(method = "getBubbleState", at = @At("RETURN"))
    private static BlockState modifyBubbleState(
        BlockState original, final Block bubbleColumn, final BlockState state,
        final BlockState occupyState
    ) {
        Block block = original.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            FluidState fluidState = occupyState.getFluidState();
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(fluidState) :
                FluidStateTranscript.transcript(fluidState)
            );
            if (fluidlogged.getValues().contains(escaped)) {
                return original.withIfExists(fluidlogged, escaped);
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "isStillWater", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/fluid/FluidState;isStill()Z"
    ))
    private static boolean modify(
        boolean original, final Block bubbleColumn,
        final BlockState occupyState
    ) {
        StringProperty fluidlogged =
        ModifyExisting.CACHED_PPTS.get(bubbleColumn);
        if (fluidlogged != null) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "isStillWater", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/fluid/FluidState;getLevel()I"
    ))
    private static int modify(
        int original, final Block bubbleColumn, final BlockState occupyState
    ) {
        StringProperty fluidlogged =
        ModifyExisting.CACHED_PPTS.get(bubbleColumn);
        if (fluidlogged != null) {
            return 8;
        }
        return original;
    }

    @WrapOperation(
        method = "update(Lnet/minecraft/block/Block;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V",
        at = @At(
            value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/world/WorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
        )
    )
    private static boolean modifyPlacedBubbleState(
        WorldAccess instance, BlockPos pos, BlockState state, int flags,
        Operation<Boolean> original
    ) {
        Block block = state.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            FluidState fluidState = instance.getFluidState(pos);
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(fluidState) :
                FluidStateTranscript.transcript(fluidState)
            );
            if (fluidlogged.getValues().contains(escaped)) {
                return original.call(instance, pos, state.withIfExists(
                    fluidlogged, escaped
                ), flags);
            }
        }
        return original.call(instance, pos, state, flags);
    }
}
