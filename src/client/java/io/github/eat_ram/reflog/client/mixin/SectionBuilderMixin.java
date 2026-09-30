package io.github.eat_ram.reflog.client.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.QuadConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.model.BlockStateModels;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder.RenderData;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

@Mixin(net.minecraft.client.render.chunk.SectionBuilder.class)
public abstract class SectionBuilderMixin {
    @Shadow
    @Final
    private boolean cutoutLeaves;
    @Shadow
    @Final
    private BlockStateModels blockModels;

    @Inject(method = "build", at = @At(
        value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/client/render/block/FluidRenderer;render(Lnet/minecraft/client/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/block/FluidRenderer$BlockVertexConsumerProvider;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)V"
    ))
    private void renderModelBasedFluidInFluidloggedBlock(
        final ChunkSectionPos sectionPos,
        final ChunkRendererRegion renderRegion,
        final VertexSorter vertexSorter,
        final BlockBufferAllocatorStorage allocatorStorage,
        CallbackInfoReturnable<RenderData> cir,
        @Local(ordinal = 0) BlockModelRenderer blockRenderer,
        @Local(ordinal = 0) QuadConsumer quadOutput,
        @Local(ordinal = 1) QuadConsumer opaqueQuadOutput,
        @Local(ordinal = 2) BlockPos pos,
        @Local(ordinal = 0) BlockState blockState,
        @Local(ordinal = 0) FluidState fluidState
    ) {
        BlockState fluidBlockState = fluidState.getBlockState();
        if (!blockState.is(fluidBlockState.getBlock()) &&
            fluidBlockState.getRenderType() == BlockRenderType.MODEL) {
            blockRenderer.renderModel(
                BlockModelRenderer
                .forceSolid(this.cutoutLeaves, fluidBlockState) ?
                opaqueQuadOutput : quadOutput,
                ChunkSectionPos.getLocalCoord(pos.getX()),
                ChunkSectionPos.getLocalCoord(pos.getY()),
                ChunkSectionPos.getLocalCoord(pos.getZ()), renderRegion, pos,
                fluidBlockState, this.blockModels.getModel(fluidBlockState),
                fluidBlockState.getRenderingSeed(pos)
            );
        }
    }
}
