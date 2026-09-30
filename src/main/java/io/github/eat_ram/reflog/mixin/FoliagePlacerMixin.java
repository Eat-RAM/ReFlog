package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.eat_ram.reflog.block.CustomFluidloggable;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.foliage.FoliagePlacer.BlockPlacer;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.world.gen.foliage.FoliagePlacer.class)
public abstract class FoliagePlacerMixin {
    @ModifyExpressionValue(
        method = "placeFoliageBlock(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/foliage/FoliagePlacer$BlockPlacer;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/world/gen/feature/TreeFeature;Lnet/minecraft/util/math/BlockPos;)Z",
        at = @At(
            value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/gen/stateprovider/BlockStateProvider;get(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"
        )
    )
    private static BlockState modifyBlockState(
        BlockState original, final StructureWorldAccess world,
        final BlockPlacer placer, final Random random,
        final TreeFeature feature, final BlockPos pos
    ) {
        Block block = original.getBlock();
        StringProperty fluidlogged =
        ModifyExisting.OVERRIDES.get(block.getClass());
        if (fluidlogged != null) {
            FluidState fluidState = world.getBlockState(pos).getFluidState();
            String escaped = FluidStateTranscript.escape(
                block instanceof Waterloggable &&
                fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                FluidStateTranscript.transcriptWaterlogged(fluidState) :
                FluidStateTranscript.transcript(fluidState)
            );
            if (fluidlogged.getValues().contains(escaped)) {
                return original.withIfExists(fluidlogged, escaped);
            }
        }
        if (block instanceof CustomFluidloggable) {
            fluidlogged =
            ((CustomFluidloggable)block).getFluidloggedProperty();
            if (fluidlogged != null) {
                FluidState fluidState =
                world.getBlockState(pos).getFluidState();
                String escaped = FluidStateTranscript.escape(
                    FluidStateTranscript.transcript(fluidState)
                );
                if (fluidlogged.getValues().contains(escaped)) {
                    return original.withIfExists(fluidlogged, escaped);
                }
            }
        }
        return original;
    }
}
