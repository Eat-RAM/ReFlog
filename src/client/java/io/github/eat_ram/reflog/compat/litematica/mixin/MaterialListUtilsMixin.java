package io.github.eat_ram.reflog.compat.litematica.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.malilib.util.data.ItemType;
import io.github.eat_ram.reflog.block.CustomFluidloggable;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.StringProperty;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

@Restriction(require = @Condition("litematica"))
@Mixin(fi.dy.masa.litematica.materials.MaterialListUtils.class)
public abstract class MaterialListUtilsMixin {
    @WrapOperation(method = "convertStatesToStacks", at = @At(
        value = "INVOKE", ordinal = 0,
        target = "Lfi/dy/masa/litematica/materials/MaterialListUtils;isWaterloggedBlock(Lnet/minecraft/block/BlockState;)Z"
    ))
    private static boolean
    handle(BlockState state, Operation<Boolean> original) {
        Block block = state.getBlock();
        return ModifyExisting.CACHED_PPTS.get(block) != null || (
            block instanceof CustomFluidloggable &&
            ((CustomFluidloggable)block).getFluidloggedProperty() != null
        ) || original.call(state);
    }

    @WrapOperation(method = "convertStatesToStacks", at = @At(
        value = "INVOKE", ordinal = 1,
        target = "Lfi/dy/masa/litematica/materials/MaterialListUtils;isWaterloggedBlock(Lnet/minecraft/block/BlockState;)Z"
    ))
    private static boolean handle(
        BlockState state, Operation<Boolean> original,
        Object2IntOpenHashMap<BlockState> blockStatesIn,
        Object2IntOpenHashMap<ItemType> itemTypesOut, MaterialCache cache
    ) {
        Block block = state.getBlock();
        if (ModifyExisting.CACHED_PPTS.get(block) != null || (
            block instanceof CustomFluidloggable &&
            ((CustomFluidloggable)block).getFluidloggedProperty() != null
        )) {
            ItemStack itm = cache.getRequiredBuildItemForState(
                state.getFluidState().getBlockState()
            );
            if (!itm.isEmpty()) {
                itemTypesOut.addTo(new ItemType(
                    itm, false, false
                ), blockStatesIn.getInt(state) * itm.getCount());
            }
            return false;
        }
        return original.call(state);
    }

    @ModifyReturnValue(method = "getBaseBlockState", at = @At("RETURN"))
    private static BlockState getBaseBlockState(BlockState original) {
        Block block = original.getBlock();
        StringProperty ppt = ModifyExisting.CACHED_PPTS.get(block);
        if (ppt != null) {
            return original.with(ppt, ppt.getValues().get(0));
        }
        if (block instanceof CustomFluidloggable) {
            ppt = ((CustomFluidloggable)block).getFluidloggedProperty();
            if (ppt != null) {
                return original.with(ppt, ppt.getValues().get(0));
            }
        }
        return original;
    }
}
