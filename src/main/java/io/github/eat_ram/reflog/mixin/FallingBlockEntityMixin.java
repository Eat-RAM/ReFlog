package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.eat_ram.reflog.block.CustomFluidloggable;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

@Mixin(net.minecraft.entity.FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin {
    @ModifyArg(method = "spawnFromBlock", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/entity/FallingBlockEntity;<init>(Lnet/minecraft/world/World;DDDLnet/minecraft/block/BlockState;)V"
    ), index = 4)
    private static BlockState removeFluidlog(BlockState blockState) {
        Block block = blockState.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            return blockState.with(
                fluidlogged, fluidlogged.getValues().get(0)
            );
        }
        if (block instanceof CustomFluidloggable) {
            fluidlogged =
            ((CustomFluidloggable)block).getFluidloggedProperty();
            if (fluidlogged != null) {
                return blockState.with(
                    fluidlogged, fluidlogged.getValues().get(0)
                );
            }
        }
        return blockState;
    }
}
