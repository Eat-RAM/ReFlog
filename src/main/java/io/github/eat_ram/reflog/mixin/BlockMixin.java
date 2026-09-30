package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;

@Mixin(net.minecraft.block.Block.class)
public abstract class BlockMixin {
    @Inject(method = "<init>", at = @At(
        value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/block/Block;appendProperties(Lnet/minecraft/state/StateManager$Builder;)V"
    ))
    private void onAppendProperties(
        final Block.Settings properties, CallbackInfo ci,
        @Local StateManager.Builder<Block, BlockState> builder
    ) {
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(this.getClass());
        if (fluidlogged != null) {
            builder.add(fluidlogged);
        }
    }
}
