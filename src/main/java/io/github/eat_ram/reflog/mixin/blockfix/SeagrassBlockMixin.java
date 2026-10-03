package io.github.eat_ram.reflog.mixin.blockfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

@Mixin(net.minecraft.block.SeagrassBlock.class)
public abstract class SeagrassBlockMixin {
    @WrapOperation(method = "getStateForNeighborUpdate", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/BlockState;isAir()Z"
    ))
    private boolean modifyBlockState(BlockState instance, Operation<Boolean> original) {
        Block block = instance.getBlock();
        StringProperty fluidlogged = ModifyExisting.CACHED_PPTS.get(block);
        if (fluidlogged != null) {
            return true;
        }
        return original.call(instance);
    }
}
