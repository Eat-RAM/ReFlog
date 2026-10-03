package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
    @Shadow
    private FluidState fluidState;

    @Shadow
    public abstract Block getBlock();

    @Shadow
    public abstract void initShapeCache();

    @Shadow
    protected abstract BlockState asBlockState();

    @ModifyReturnValue(method = "getLuminance", at = @At("RETURN"))
    private int modifyLuminance(int original) {
        if (this.fluidState != null && !this.fluidState.isEmpty()) {
            BlockState fluidBlockState = this.fluidState.getBlockState();
            if (fluidBlockState != (Object)this) {
                return Math.max(original, fluidBlockState.getLuminance());
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getFluidState", at = @At("RETURN"))
    private FluidState modifyFluidState(FluidState original) {
        if (this.fluidState != null &&
            ((AbstractBlockAccessor)this.getBlock())
            .invokeGetFluidState(this.asBlockState()) != original) {
            this.initShapeCache();
        }
        return original;
    }
}
