package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BubbleColumnBlock;

@Mixin(net.minecraft.block.FluidBlock.class)
public abstract class FluidBlockMixin {
    @ModifyExpressionValue(method = "bubbleColumnCanOccupy", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/fluid/FluidState;isStill()Z"
    ))
    private static boolean modify(boolean original, final BlockState state) {
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(BubbleColumnBlock.class);
        if (fluidlogged != null) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "bubbleColumnCanOccupy", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/fluid/FluidState;isFull()Z"
    ))
    private static boolean modify2(boolean original, final BlockState state) {
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(BubbleColumnBlock.class);
        if (fluidlogged != null) {
            return true;
        }
        return original;
    }
}
