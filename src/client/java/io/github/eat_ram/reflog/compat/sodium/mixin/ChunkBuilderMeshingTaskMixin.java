package io.github.eat_ram.reflog.compat.sodium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline
       .BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline
       .BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks
       .ChunkBuilderMeshingTask;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.model.BlockStateModel;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

@Restriction(require = @Condition("sodium"))
@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin {
    @Inject(
        method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(
            value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/block/BlockState;getFluidState()Lnet/minecraft/fluid/FluidState;"
        )
    )
    private void renderModelBasedFluidInSodium(
        CallbackInfoReturnable<ChunkBuildOutput> cir,
        @Local(ordinal = 0) BlockRenderCache cache,
        @Local(ordinal = 0) BlockPos.Mutable blockPos,
        @Local(ordinal = 1) BlockPos.Mutable modelOffset,
        @Local(ordinal = 0) BlockRenderer blockRenderer,
        @Local(ordinal = 0) BlockState blockState
    ) {
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty()) {
            BlockState fluidBlockState = fluidState.getBlockState();
            if (!blockState.is(fluidBlockState.getBlock()) &&
                fluidBlockState.getRenderType() == BlockRenderType.MODEL) {
                BlockStateModel model =
                    cache.getBlockModels().getModel(fluidBlockState);
                blockRenderer.renderModel(
                    model, fluidBlockState, blockPos, modelOffset
                );
            }
        }
    }
}
