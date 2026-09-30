package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eat_ram.reflog.block.CustomFluidloggable;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.fluid.FlowableFluid.class)
public abstract class FlowableFluidMixin {
    @Inject(method = "flow", at = @At(
        value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER,
        target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"
    ), cancellable = true)
    private void flow(
        final WorldAccess world, final BlockPos pos, final BlockState state,
        final Direction direction, final FluidState fluidState, CallbackInfo ci
    ) {
        Block block = state.getBlock();
        if (ModifyExisting.RETAIN_FLUIDS_IN.contains(block.getClass())) {
            ci.cancel();
        }
        StringProperty ppt = ModifyExisting.OVERRIDES.get(block.getClass());
        if (ppt != null) {
            if (!world.isClient()) {
                String escaped = FluidStateTranscript.escape((
                    block instanceof Waterloggable &&
                    ppt.getName().equals(WATERLOGGED.getName())
                ) ? FluidStateTranscript.transcriptWaterlogged(fluidState) :
                    FluidStateTranscript.transcript(fluidState)
                );
                if (!state.get(ppt).equals(escaped) &&
                    ppt.getValues().contains(escaped)) {
                    world.setBlockState(pos, state.with(ppt, escaped));
                    world.scheduleFluidTick(
                        pos, fluidState.getFluid(),
                        fluidState.getFluid().getTickRate(world)
                    );
                }
            }
            ci.cancel();
        }
    }

    @WrapOperation(method = "tryFlow", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/fluid/FlowableFluid;canFillWithFluid(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/Fluid;)Z"
    ))
    private static boolean canFillWithFluid(
        BlockView world, BlockPos pos, BlockState state, Fluid fluid,
        Operation<Boolean> original,
        @Local(ordinal = 2) FluidState newBelowFluid
    ) {
        Block block = state.getBlock();
        if (ModifyExisting.RETAIN_FLUIDS_IN.contains(block.getClass())) {
            return false;
        }
        StringProperty ppt = ModifyExisting.OVERRIDES.get(block.getClass());
        if (ppt != null) {
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                ppt.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(newBelowFluid) :
                FluidStateTranscript.transcript(newBelowFluid)
            );
            return ppt.getValues().contains(escaped);
        }
        if (block instanceof CustomFluidloggable) {
            return ((CustomFluidloggable)block).canFillWithFluidState(
                null, world, pos, state, newBelowFluid
            );
        }
        return original.call(world, pos, state, fluid);
    }

    @WrapOperation(method = "canFlowThrough(Lnet/minecraft/world/BlockView;Lnet/minecraft/fluid/Fluid;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Z", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/fluid/FlowableFluid;canFillWithFluid(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/Fluid;)Z"
    ))
    private static boolean canFillWithFluid(
        BlockView world, BlockPos pos, BlockState state, Fluid fluid,
        Operation<Boolean> original, final BlockView world2,
        final Fluid fluid2, final BlockPos pos2, final BlockState state2,
        final Direction face, final BlockPos fromPos,
        final BlockState fromState, final FluidState fluidState
    ) {
        Block block = state.getBlock();
        if (ModifyExisting.RETAIN_FLUIDS_IN.contains(block.getClass())) {
            return false;
        }
        StringProperty ppt = ModifyExisting.OVERRIDES.get(block.getClass());
        if (ppt != null) {
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                ppt.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(fluidState) :
                FluidStateTranscript.transcript(fluidState)
            );
            return ppt.getValues().contains(escaped);
        }
        if (block instanceof CustomFluidloggable) {
            return ((CustomFluidloggable)block).canFillWithFluidState(
                null, world, pos, state, fluidState
            );
        }
        return original.call(world, pos, state, fluid);
    }

    @WrapOperation(method = "getSpread", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/fluid/FlowableFluid;canFillWithFluid(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/Fluid;)Z"
    ))
    private static boolean canFillWithFluid2(
        BlockView world, BlockPos pos, BlockState state, Fluid fluid,
        Operation<Boolean> original,
        @Local(ordinal = 1) FluidState newFluid
    ) {
        Block block = state.getBlock();
        if (ModifyExisting.RETAIN_FLUIDS_IN.contains(block.getClass())) {
            return false;
        }
        StringProperty ppt = ModifyExisting.OVERRIDES.get(block.getClass());
        if (ppt != null) {
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                ppt.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(newFluid) :
                FluidStateTranscript.transcript(newFluid)
            );
            return ppt.getValues().contains(escaped);
        }
        if (block instanceof CustomFluidloggable) {
            return ((CustomFluidloggable)block).canFillWithFluidState(
                null, world, pos, state, newFluid
            );
        }
        return original.call(world, pos, state, fluid);
    }

    @ModifyReturnValue(
        method = "canFill(Lnet/minecraft/block/BlockState;)Z",
        at = @At("RETURN")
    )
    private static boolean canFill(boolean original, final BlockState state) {
        if (!original) {
            Block block = state.getBlock();
            StringProperty ppt =
            ModifyExisting.OVERRIDES.get(block.getClass());
            if (ppt != null) {
                return true;
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "onScheduledTick", at = {@At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"
    ), @At(
        value = "INVOKE",
        target = "Lnet/minecraft/fluid/FluidState;getBlockState()Lnet/minecraft/block/BlockState;"
    )})
    private BlockState retainState(
        BlockState original, final ServerWorld world, final BlockPos pos,
        BlockState blockState, FluidState fluidState
    ) {
        Block block = blockState.getBlock();
        fluidState = original.getFluidState();
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(block.getClass());
        if (fluidlogged != null) {
            String escaped = FluidStateTranscript.escape((
                block instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName())
            ) ? FluidStateTranscript.transcriptWaterlogged(fluidState) :
                FluidStateTranscript.transcript(fluidState)
            );
            return fluidlogged.getValues().contains(escaped) ?
                   blockState.with(fluidlogged, escaped) : blockState;
        }
        if (block instanceof CustomFluidloggable) {
            fluidlogged =
            ((CustomFluidloggable)block).getFluidloggedProperty();
            if (fluidlogged != null) {
                String escaped = FluidStateTranscript.escape((
                    block instanceof Waterloggable &&
                    fluidlogged.getName().equals(WATERLOGGED.getName())
                ) ? FluidStateTranscript.transcriptWaterlogged(fluidState) :
                    FluidStateTranscript.transcript(fluidState)
                );
                return fluidlogged.getValues().contains(escaped) ?
                       blockState.with(fluidlogged, escaped) : blockState;
            }
        }
        return original;
    }

    @Inject(method = "getUpdatedState", at = @At("HEAD"), cancellable = true)
    private void getUpdatedState(
        final ServerWorld world, final BlockPos pos, final BlockState state,
        CallbackInfoReturnable<FluidState> cir
    ) {
        Block block = state.getBlock();
        if (ModifyExisting.RETAIN_FLUIDS_IN.contains(block.getClass())) {
            cir.setReturnValue(state.getFluidState());
        }
    }
}
