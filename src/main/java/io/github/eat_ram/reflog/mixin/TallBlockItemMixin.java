package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;

@Mixin(net.minecraft.item.TallBlockItem.class)
public abstract class TallBlockItemMixin extends BlockItem {
    private TallBlockItemMixin() {
        super(null, null);
        throw new AssertionError();
    }

    @ModifyExpressionValue(method = "place", at = {@At(
        value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"
    ), @At(
        value = "INVOKE", ordinal = 1,
        target = "Lnet/minecraft/block/Block;getDefaultState()Lnet/minecraft/block/BlockState;"
    )})
    private BlockState modifyBlockState(
        BlockState original, final ItemPlacementContext context,
        final BlockState state, @Local(ordinal = 0) BlockPos above
    ) {
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(this.getBlock().getClass());
        if (fluidlogged != null) {
            return context.getWorld().getFluidState(above).getBlockState();
        }
        return original;
    }
}
