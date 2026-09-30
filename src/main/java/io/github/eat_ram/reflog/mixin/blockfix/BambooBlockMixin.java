package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.BambooBlock.class)
public abstract class BambooBlockMixin {
    @ModifyExpressionValue(method = "getPlacementState", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/fluid/FluidState;isEmpty()Z"
    ))
    private boolean modifyFluidState(
        boolean original, @Local(ordinal = 0) FluidState fluidState
    ) {
        if (original) {
            return true;
        }
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(this.getClass());
        if (fluidlogged != null) {
            String escaped = FluidStateTranscript.escape(
                this instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(fluidState) :
                FluidStateTranscript.transcript(fluidState)
            );
            return fluidlogged.getValues().contains(escaped);
        }
        return original;
    }

    @WrapOperation(method = "grow", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/server/world/ServerWorld;isAir(Lnet/minecraft/util/math/BlockPos;)Z"
    ))
    private boolean growToFluid(
        ServerWorld instance, BlockPos pos, Operation<Boolean> original
    ) {
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(this.getClass());
        if (fluidlogged != null) {
            BlockState blockState = instance.getBlockState(pos);
            FluidState fluidState = blockState.getFluidState();
            if (fluidState.getBlockState() == blockState) {
                String escaped = FluidStateTranscript.escape(
                    this instanceof Waterloggable &&
                    fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                    FluidStateTranscript.transcriptWaterlogged(fluidState) :
                    FluidStateTranscript.transcript(fluidState)
                );
                return fluidlogged.getValues().contains(escaped);
            }
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "randomTick", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/server/world/ServerWorld;isAir(Lnet/minecraft/util/math/BlockPos;)Z"
    ))
    private boolean growInFluid(
        ServerWorld instance, BlockPos pos, Operation<Boolean> original
    ) {
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(this.getClass());
        if (fluidlogged != null) {
            BlockState blockState = instance.getBlockState(pos);
            FluidState fluidState = blockState.getFluidState();
            if (fluidState.getBlockState() == blockState) {
                String escaped = FluidStateTranscript.escape(
                    this instanceof Waterloggable &&
                    fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                    FluidStateTranscript.transcriptWaterlogged(fluidState) :
                    FluidStateTranscript.transcript(fluidState)
                );
                return fluidlogged.getValues().contains(escaped);
            }
        }
        return original.call(instance, pos);
    }

    @ModifyExpressionValue(method = "updateLeaves", at = @At(
        value = "INVOKE", ordinal = 4,
        target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
    ))
    private Object growToFluid(
        Object original, final BlockState state, final World world,
        final BlockPos pos, final Random random, final int height
    ) {
        BlockState nstate = (BlockState)original;
        Block block = nstate.getBlock();
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(block.getClass());
        if (fluidlogged != null) {
            FluidState fluidState = world.getFluidState(pos.up());
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(fluidState) :
                FluidStateTranscript.transcript(fluidState)
            );
            if (fluidlogged.getValues().contains(escaped)) {
                return nstate.withIfExists(fluidlogged, escaped);
            }
        }
        return original;
    }
}
