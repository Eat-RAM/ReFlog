package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

@Mixin(net.minecraft.block.AbstractBlock.class)
public interface AbstractBlockAccessor {
    @Invoker("getFluidState")
    public FluidState invokeGetFluidState(final BlockState state);

    @Mixin(net.minecraft.block.AbstractBlock.Settings.class)
    public static interface Settings {
        @Accessor("registryKey")
        public @Nullable RegistryKey<Block> getRegistryKey();
    }
}
