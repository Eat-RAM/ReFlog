package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.AbstractBedBlock.class)
public abstract class AbstractBedBlockMixin {
    @ModifyExpressionValue(method = "onPlaced", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/block/BlockState;with(Lnet/minecraft/state/property/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
    ))
    private Object modifyBlockState(
        Object original,
        @Local(ordinal = 0, argsOnly = true) final World world,
        @Local(ordinal = 1) BlockPos otherPos
    ) {
        BlockState state = (BlockState)original;
        Block block = state.getBlock();
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(block.getClass());
        if (fluidlogged != null) {
            FluidState otherFluidState =
            world.getBlockState(otherPos).getFluidState();
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(otherFluidState) :
                FluidStateTranscript.transcript(otherFluidState)
            );
            if (fluidlogged.getValues().contains(escaped)) {
                return state.withIfExists(fluidlogged, escaped);
            }
        }
        return original;
    }
}
