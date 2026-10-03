package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {
    @ModifyReturnValue(
        method = "getPlacementState(Lnet/minecraft/world/BlockView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
        at = @At("RETURN")
    )
    private BlockState modifyBlockState(
        BlockState original, final BlockView world, BlockState state,
        final BlockPos pos
    ) {
        Block block = original.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            FluidState fluidState = world.getFluidState(pos);
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

    @ModifyExpressionValue(method = "onUse", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
    ))
    private Object modifyBlockState(
        Object original, final BlockState state, final World world,
        final BlockPos pos, final PlayerEntity player, final BlockHitResult hit
    ) {
        BlockState nstate = (BlockState)original;
        Block block = nstate.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            FluidState fluidState = state.getFluidState();
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
