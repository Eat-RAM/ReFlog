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
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.TallPlantBlock.class)
public abstract class TallPlantBlockMixin {
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

    @ModifyExpressionValue(method = "afterBreak", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"
    ))
    private BlockState modifyBlockState(
        BlockState original, final ServerWorld level,
        final ServerPlayerEntity player, final BlockPos pos,
        final BlockState state, final @Nullable BlockEntity blockEntity,
        final ItemStack tool
    ) {
        if (!original.isAir()) {
            return original;
        }
        Block block = state.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
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
                return fstate.getBlockState();
            }
        }
        return original;
    }
}
