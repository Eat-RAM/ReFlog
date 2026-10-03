package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.DoorBlock.class)
public abstract class DoorBlockMixin {
    @WrapOperation(method = "onPlaced", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"
    ))
    private boolean modifyBlockState(
        World instance, BlockPos pos, BlockState state,
        Operation<Boolean> original
    ) {
        Block block = state.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            FluidState otherFluidState = instance.getFluidState(pos);
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(otherFluidState) :
                FluidStateTranscript.transcript(otherFluidState)
            );
            if (fluidlogged.getValues().contains(escaped)) {
                return original.call(instance, pos, state.withIfExists(
                    fluidlogged, escaped
                ));
            }
        }
        return original.call(instance, pos, state);
    }

    @ModifyExpressionValue(method = "getStateForNeighborUpdate", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
    ))
    private Object modifyBlockState(
        Object original, final BlockState state, final WorldView world,
        final ScheduledTickView tickView, final BlockPos pos,
        final Direction direction, final BlockPos neighborPos,
        final BlockState neighborState, final Random random
    ) {
        BlockState nstate = (BlockState)original;
        Block block = nstate.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            FluidState fluidState = world.getBlockState(pos).getFluidState();
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
