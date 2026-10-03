package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin({
    net.minecraft.block.AbstractBedBlock.class,
    net.minecraft.block.AbstractBlock.class,
    net.minecraft.block.AbstractCoralBlock.class,
    net.minecraft.block.AbstractPlantBlock.class,
    net.minecraft.block.AbstractPlantStemBlock.class,
    net.minecraft.block.AbstractPressurePlateBlock.class,
    net.minecraft.block.AbstractRailBlock.class,
    net.minecraft.block.AbstractSignBlock.class,
    net.minecraft.block.AbstractTorchBlock.class,
    net.minecraft.block.AmethystClusterBlock.class,
    net.minecraft.block.AttachedStemBlock.class,
    net.minecraft.block.BambooBlock.class,
    net.minecraft.block.BambooShootBlock.class,
    net.minecraft.block.BannerBlock.class,
    net.minecraft.block.BarrierBlock.class,
    net.minecraft.block.BeehiveBlock.class,
    net.minecraft.block.BellBlock.class,
    net.minecraft.block.BigDripleafBlock.class,
    net.minecraft.block.BigDripleafStemBlock.class,
    net.minecraft.block.BrushableBlock.class,
    net.minecraft.block.CactusBlock.class,
    net.minecraft.block.CakeBlock.class,
    net.minecraft.block.CampfireBlock.class,
    net.minecraft.block.CandleBlock.class,
    net.minecraft.block.CandleCakeBlock.class,
    net.minecraft.block.CarpetBlock.class,
    net.minecraft.block.ChainBlock.class,
    net.minecraft.block.ChestBlock.class,
    net.minecraft.block.ChorusFlowerBlock.class,
    net.minecraft.block.ChorusPlantBlock.class,
    net.minecraft.block.CocoaBlock.class,
    net.minecraft.block.ComparatorBlock.class,
    net.minecraft.block.ConcretePowderBlock.class,
    net.minecraft.block.ConduitBlock.class,
    net.minecraft.block.CopperChestBlock.class,
    net.minecraft.block.CopperGolemStatueBlock.class,
    net.minecraft.block.CoralBlock.class,
    net.minecraft.block.CoralBlockBlock.class,
    net.minecraft.block.CoralFanBlock.class,
    net.minecraft.block.CoralWallFanBlock.class,
    net.minecraft.block.CreakingHeartBlock.class,
    net.minecraft.block.DeadCoralWallFanBlock.class,
    net.minecraft.block.DecoratedPotBlock.class,
    net.minecraft.block.DirtPathBlock.class,
    net.minecraft.block.DoorBlock.class,
    net.minecraft.block.DriedGhastBlock.class,
    net.minecraft.block.EnderChestBlock.class,
    net.minecraft.block.FallingBlock.class,
    net.minecraft.block.FarmlandBlock.class,
    net.minecraft.block.FenceBlock.class,
    net.minecraft.block.FenceGateBlock.class,
    net.minecraft.block.FireBlock.class,
    net.minecraft.block.FlowerPotBlock.class,
    net.minecraft.block.FluidBlock.class,
    net.minecraft.block.FrogspawnBlock.class,
    net.minecraft.block.GrateBlock.class,
    net.minecraft.block.HangingMossBlock.class,
    net.minecraft.block.HangingRootsBlock.class,
    net.minecraft.block.HangingSignBlock.class,
    net.minecraft.block.HeavyCoreBlock.class,
    net.minecraft.block.LadderBlock.class,
    net.minecraft.block.LanternBlock.class,
    net.minecraft.block.LeavesBlock.class,
    net.minecraft.block.LightBlock.class,
    net.minecraft.block.LightningRodBlock.class,
    net.minecraft.block.MangroveRootsBlock.class,
    net.minecraft.block.MultifaceBlock.class,
    net.minecraft.block.MushroomBlock.class,
    net.minecraft.block.NetherPortalBlock.class,
    net.minecraft.block.NoteBlock.class,
    net.minecraft.block.ObserverBlock.class,
    net.minecraft.block.PaleMossCarpetBlock.class,
    net.minecraft.block.PaneBlock.class,
    net.minecraft.block.PistonHeadBlock.class,
    net.minecraft.block.PitcherCropBlock.class,
    net.minecraft.block.PlantBlock.class,
    net.minecraft.block.PotentSulfurBlock.class,
    net.minecraft.block.PropaguleBlock.class,
    net.minecraft.block.RedstoneWireBlock.class,
    net.minecraft.block.RepeaterBlock.class,
    net.minecraft.block.ScaffoldingBlock.class,
    net.minecraft.block.SculkSensorBlock.class,
    net.minecraft.block.SculkShriekerBlock.class,
    net.minecraft.block.SeagrassBlock.class,
    net.minecraft.block.SeaPickleBlock.class,
    net.minecraft.block.ShelfBlock.class,
    net.minecraft.block.ShelfMushroomBlock.class,
    net.minecraft.block.SignBlock.class,
    net.minecraft.block.SlabBlock.class,
    net.minecraft.block.SmallDripleafBlock.class,
    net.minecraft.block.SnowBlock.class,
    net.minecraft.block.SnowyBlock.class,
    net.minecraft.block.SoulFireBlock.class,
    net.minecraft.block.SpeleothemBlock.class,
    net.minecraft.block.SporeBlossomBlock.class,
    net.minecraft.block.StairsBlock.class,
    net.minecraft.block.SugarCaneBlock.class,
    net.minecraft.block.TallPlantBlock.class,
    net.minecraft.block.TrapdoorBlock.class,
    net.minecraft.block.TripwireBlock.class,
    net.minecraft.block.TripwireHookBlock.class,
    net.minecraft.block.VineBlock.class,
    net.minecraft.block.WallBannerBlock.class,
    net.minecraft.block.WallBlock.class,
    net.minecraft.block.WallHangingSignBlock.class,
    net.minecraft.block.WallMountedBlock.class,
    net.minecraft.block.WallRedstoneTorchBlock.class,
    net.minecraft.block.WallSignBlock.class,
    net.minecraft.block.WallTorchBlock.class
})
public abstract class MultipleMixins1 {
    @ModifyReturnValue(method = "getStateForNeighborUpdate", at = @At("RETURN"))
    private BlockState tickFluid(
        BlockState original, BlockState state, WorldView world,
        ScheduledTickView tickView, BlockPos pos, Direction direction,
        BlockPos neighborPos, BlockState neighborState, Random random
    ) {
        StringProperty fluidlogged =
        ModifyExisting.CACHED_PPTS.get(original.getBlock());
        if (fluidlogged != null &&
            !original.getOrEmpty(WATERLOGGED).orElse(false)) {
            Fluid fluid = (this instanceof Waterloggable &&
                           fluidlogged.getName().equals(WATERLOGGED.getName())?
                           FluidStateTranscript.restoreWaterlogged(
                FluidStateTranscript.unescape(original.get(fluidlogged)), true
            ) : FluidStateTranscript.restore(
                FluidStateTranscript.unescape(original.get(fluidlogged)), true
            )).getFluid();
            tickView.scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
        }
        return original;
    }
}
