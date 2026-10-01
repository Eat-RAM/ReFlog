package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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
        ModifyExisting.OVERRIDES.get(state.getBlock().getClass());
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
        ModifyExisting.OVERRIDES.get(state.getBlock().getClass());
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
        ModifyExisting.OVERRIDES.get(occupyState.getBlock().getClass());
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
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(block.getClass());
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
        ModifyExisting.OVERRIDES.get(bubbleColumn.getClass());
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
        ModifyExisting.OVERRIDES.get(bubbleColumn.getClass());
        if (fluidlogged != null) {
            return 8;
        }
        return original;
    }

    @ModifyArgs(
        method = "update(Lnet/minecraft/block/Block;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V",
        at = @At(
            value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/world/WorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
        )
    )
    private static void modifyPlacedBubbleState(
        Args args, final Block bubbleColumn, final WorldAccess level,
        final BlockPos occupyAt, final BlockState occupyState,
        final BlockState belowState
    ) {
        BlockState state = args.get(1);
        Block block = state.getBlock();
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(block.getClass());
        if (fluidlogged != null) {
            FluidState fluidState = level.getFluidState(args.get(0));
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(fluidState) :
                FluidStateTranscript.transcript(fluidState)
            );
            if (fluidlogged.getValues().contains(escaped)) {
                args.set(1, state.withIfExists(fluidlogged, escaped));
            }
        }
    }
}
