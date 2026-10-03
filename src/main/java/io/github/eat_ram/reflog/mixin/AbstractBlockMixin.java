package io.github.eat_ram.reflog.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(net.minecraft.block.AbstractBlock.class)
public abstract class AbstractBlockMixin {
    @ModifyReturnValue(method = "getFluidState", at = @At("RETURN"))
    private FluidState modifyFluidState(FluidState original, BlockState state) {
        if ((Object)this instanceof Block) {
            StringProperty fluidlogged =
            ModifyExisting.CACHED_PPTS.get((Block)(Object)this);
            if (fluidlogged != null) {
                Optional<String> fs = state.getOrEmpty(fluidlogged);
                if (fs.isPresent()) {
                    return this instanceof Waterloggable &&
                           fluidlogged.getName().equals(WATERLOGGED.getName())?
                           FluidStateTranscript.restoreWaterlogged(
                               FluidStateTranscript.unescape(fs.get()), true
                           ) : FluidStateTranscript.restore(
                        FluidStateTranscript.unescape(fs.get()), true
                    );
                }
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getStateForNeighborUpdate", at = @At("RETURN"))
    private BlockState modifyUpdatedState(
        BlockState original, final BlockState state, final WorldView world,
        final ScheduledTickView tickView, final BlockPos pos,
        final Direction direction, final BlockPos neighborPos,
        final BlockState neighborState, final Random random
    ) {
        if ((Object)this instanceof Block) {
            StringProperty fluidlogged =
            ModifyExisting.CACHED_PPTS.get((Block)(Object)this);
            if (fluidlogged != null) {
                Optional<String> fs = state.getOrEmpty(fluidlogged);
                if (fs.isPresent()) {
                    Fluid fluid = (
                        this instanceof Waterloggable &&
                        fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                        FluidStateTranscript.restoreWaterlogged(
                            FluidStateTranscript.unescape(fs.get()), true
                        ) : FluidStateTranscript.restore(
                            FluidStateTranscript.unescape(fs.get()), true
                        )
                    ).getFluid();
                    tickView
                    .scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
                }
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "canReplace", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/block/BlockState;isReplaceable()Z"
    ))
    private boolean canReplace(
        boolean original, final BlockState state,
        final ItemPlacementContext context
    ) {
        if (original) {
            return true;
        }
        ItemStack stack = context.getStack();
        if (stack != null && !stack.isEmpty()) {
            Item item = stack.getItem();
            if (item instanceof BlockItem &&
                state.getFluidState().getBlockState() == state) {
                Block block = ((BlockItem)item).getBlock();
                StringProperty fluidlogged =
                ModifyExisting.CACHED_PPTS.get(block);
                if (fluidlogged != null) {
                    String escaped = FluidStateTranscript.escape(
                        block instanceof Waterloggable &&
                        fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                        FluidStateTranscript
                        .transcriptWaterlogged(state.getFluidState()) :
                        FluidStateTranscript.transcript(state.getFluidState())
                    );
                    return fluidlogged.getValues().contains(escaped);
                }
            }
        }
        return original;
    }
}
