package io.github.eat_ram.reflog.mixin.blockfix;

import java.util.Optional;

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
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.BambooShootBlock.class)
public abstract class BambooShootBlockMixin {
    @ModifyExpressionValue(method = "getStateForNeighborUpdate", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"
    ))
    private BlockState modifyBlockState(
        BlockState original, final BlockState state, final WorldView world,
        final ScheduledTickView tickView, final BlockPos pos,
        final Direction direction, final BlockPos neighborPos,
        final BlockState neighborState, final Random random
    ) {
        if (!original.isAir()) {
            return original;
        }
        Block block = state.getBlock();
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(block.getClass());
        if (fluidlogged != null) {
            Optional<String> fs = state.getOrEmpty(fluidlogged);
            if (fs.isPresent()) {
                FluidState fstate = (
                    block instanceof Waterloggable &&
                    fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                    FluidStateTranscript.restoreWaterlogged(
                        FluidStateTranscript.unescape(fs.get()), true
                    ) : FluidStateTranscript.restore(
                        FluidStateTranscript.unescape(fs.get()), true
                    )
                );
                Fluid fluid = fstate.getFluid();
                tickView
                .scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
                return fstate.getBlockState();
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "getStateForNeighborUpdate", at = @At(
        value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"
    ))
    private BlockState growInFluid(
        BlockState original, final BlockState state, final WorldView world,
        final ScheduledTickView tickView, final BlockPos pos,
        final Direction direction, final BlockPos neighborPos,
        final BlockState neighborState, final Random random
    ) {
        Block block = original.getBlock();
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(block.getClass());
        if (fluidlogged != null) {
            FluidState fluidState = state.getFluidState();
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

    @ModifyExpressionValue(method = "grow(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
    ))
    private Object growToFluid(
        Object original, final World world, final BlockPos pos
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
