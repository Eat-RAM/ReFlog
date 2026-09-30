package io.github.eat_ram.reflog.block;

import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface CustomFluidloggable extends FluidFillable, FluidDrainable {
    public Iterable<String> getAvailableFluidStates();

    public StringProperty getFluidloggedProperty();

    public default boolean canFillWithFluidState(
        @Nullable LivingEntity filler, BlockView world, BlockPos pos,
        BlockState state, FluidState fluidState
    ) {
        String escaped = FluidStateTranscript.escape(
            FluidStateTranscript.transcript(fluidState)
        );
        for (String s : this.getAvailableFluidStates()) {
            if (escaped.equals(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public default boolean canFillWithFluid(
        @Nullable LivingEntity filler, BlockView world, BlockPos pos,
        BlockState state, Fluid fluid
    ) {
        return this.canFillWithFluidState(
            filler, world, pos, state, fluid.getDefaultState()
        );
    }

    @Override
    public default boolean tryFillWithFluid(
        WorldAccess world, BlockPos pos, BlockState state,
        FluidState fluidState
    ) {
        StringProperty ppt = this.getFluidloggedProperty();
        if (ppt != null && !world.isClient()) {
            String escaped = FluidStateTranscript.escape(
                FluidStateTranscript.transcript(fluidState)
            );
            if (!state.get(ppt).equals(escaped)) {
                world.setBlockState(pos, state.with(ppt, escaped));
                world.scheduleFluidTick(
                    pos, fluidState.getFluid(),
                    fluidState.getFluid().getTickRate(world)
                );
                return true;
            }
        }
        return false;
    }

    @Override
    public default ItemStack tryDrainFluid(
        @Nullable LivingEntity drainer, WorldAccess world, BlockPos pos,
        BlockState state
    ) {
        StringProperty ppt = this.getFluidloggedProperty();
        if (ppt != null) {
            FluidState fstate = FluidStateTranscript.restore(
                FluidStateTranscript.unescape(state.get(ppt)), true
            );
            if (fstate.isStill()) {
                Item bucketItem = fstate.getFluid().getBucketItem();
                if (bucketItem != null) {
                    BlockState newState =
                    state.with(ppt, ppt.getValues().get(0));
                    world.setBlockState(pos, newState);
                    if (!newState.canPlaceAt(world, pos)) {
                        world.breakBlock(pos, true);
                    }
                    return new ItemStack(bucketItem);
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
