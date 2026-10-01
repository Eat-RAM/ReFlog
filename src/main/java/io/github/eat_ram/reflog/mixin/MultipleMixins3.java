package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

@Mixin({
    net.minecraft.block.BubbleColumnBlock.class,
    net.minecraft.block.KelpBlock.class,
    net.minecraft.block.KelpPlantBlock.class,
    net.minecraft.block.SeagrassBlock.class,
    net.minecraft.block.TallSeagrassBlock.class,
})
public abstract class MultipleMixins3 {
    @ModifyReturnValue(method = "getFluidState", at = @At("RETURN"))
    private FluidState
    modifyFluidState(FluidState original, BlockState state) {
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(state.getBlock().getClass());
        if (fluidlogged != null) {
            return FluidStateTranscript.restore(
                FluidStateTranscript.unescape(state.get(fluidlogged)), true
            );
        }
        return original;
    }
}
