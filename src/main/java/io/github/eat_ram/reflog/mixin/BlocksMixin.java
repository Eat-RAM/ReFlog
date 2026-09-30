package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.eat_ram.reflog.Config;

@Mixin(net.minecraft.block.Blocks.class)
public abstract class BlocksMixin {
    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void inject(CallbackInfo ci) {
        Config.init();
    }
}
