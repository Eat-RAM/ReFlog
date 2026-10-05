package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.SeagrassBlock.class)
public abstract class SeagrassBlockMixin {
    @WrapOperation(method = "getStateForNeighborUpdate", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/BlockState;isAir()Z"
    ))
    private boolean
    modifyBlockState(BlockState instance, Operation<Boolean> original) {
        Block block = instance.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            return true;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "isFertilizable", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/block/BlockState;is(Ljava/lang/Object;)Z"
    ))
    private boolean growToFluid(
        BlockState instance, Object o, Operation<Boolean> original
    ) {
        StringProperty fluidlogged =
        ModifyExisting.CACHED_PPTS.get(Blocks.TALL_SEAGRASS);
        if (fluidlogged != null) {
            FluidState fluidState = instance.getFluidState();
            if (fluidState.getBlockState() == instance) {
                String escaped = FluidStateTranscript.escape(
                    this instanceof Waterloggable &&
                    fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                    FluidStateTranscript.transcriptWaterlogged(fluidState) :
                    FluidStateTranscript.transcript(fluidState)
                );
                return fluidlogged.getValues().contains(escaped);
            }
        }
        return original.call(instance, o);
    }

    @WrapOperation(method = "grow", at = {@At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
    ), @At(
        value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
    )})
    private boolean growInFluid(
        ServerWorld instance, BlockPos pos, BlockState state, int flags,
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
