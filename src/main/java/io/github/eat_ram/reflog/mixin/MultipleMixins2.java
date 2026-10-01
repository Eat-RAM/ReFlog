package io.github.eat_ram.reflog.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eat_ram.reflog.block.ModifyExisting;
import io.github.eat_ram.reflog.state.FluidStateTranscript;
import io.github.eat_ram.reflog.state.StringProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin({
    net.minecraft.block.AbstractBedBlock.class,
    net.minecraft.block.AbstractCoralBlock.class,
    net.minecraft.block.AbstractFireBlock.class,
    net.minecraft.block.AbstractFurnaceBlock.class,
    net.minecraft.block.AbstractPlantPartBlock.class,
    net.minecraft.block.AbstractRailBlock.class,
    net.minecraft.block.AbstractRedstoneGateBlock.class,
    net.minecraft.block.AbstractSkullBlock.class,
    net.minecraft.block.AmethystClusterBlock.class,
    net.minecraft.block.AnvilBlock.class,
    net.minecraft.block.BambooBlock.class,
    net.minecraft.block.BannerBlock.class,
    net.minecraft.block.BarrelBlock.class,
    net.minecraft.block.BarrierBlock.class,
    net.minecraft.block.BeehiveBlock.class,
    net.minecraft.block.BellBlock.class,
    net.minecraft.block.BigDripleafBlock.class,
    net.minecraft.block.Block.class,
    net.minecraft.block.CalibratedSculkSensorBlock.class,
    net.minecraft.block.CampfireBlock.class,
    net.minecraft.block.CandleBlock.class,
    net.minecraft.block.CarvedPumpkinBlock.class,
    net.minecraft.block.ChainBlock.class,
    net.minecraft.block.ChestBlock.class,
    net.minecraft.block.ChiseledBookshelfBlock.class,
    net.minecraft.block.ChorusPlantBlock.class,
    net.minecraft.block.CocoaBlock.class,
    net.minecraft.block.CommandBlock.class,
    net.minecraft.block.ConcretePowderBlock.class,
    net.minecraft.block.ConduitBlock.class,
    net.minecraft.block.CopperChestBlock.class,
    net.minecraft.block.CopperGolemStatueBlock.class,
    net.minecraft.block.CoralBlockBlock.class,
    net.minecraft.block.CrafterBlock.class,
    net.minecraft.block.CreakingHeartBlock.class,
    net.minecraft.block.DeadCoralWallFanBlock.class,
    net.minecraft.block.DecoratedPotBlock.class,
    net.minecraft.block.DirtPathBlock.class,
    net.minecraft.block.DispenserBlock.class,
    net.minecraft.block.DoorBlock.class,
    net.minecraft.block.DriedGhastBlock.class,
    net.minecraft.block.EnderChestBlock.class,
    net.minecraft.block.EndPortalFrameBlock.class,
    net.minecraft.block.EndRodBlock.class,
    net.minecraft.block.FarmlandBlock.class,
    net.minecraft.block.FenceBlock.class,
    net.minecraft.block.FenceGateBlock.class,
    net.minecraft.block.FireBlock.class,
    net.minecraft.block.FlowerbedBlock.class,
    net.minecraft.block.GlazedTerracottaBlock.class,
    net.minecraft.block.GrateBlock.class,
    net.minecraft.block.HangingRootsBlock.class,
    net.minecraft.block.HangingSignBlock.class,
    net.minecraft.block.HeavyCoreBlock.class,
    net.minecraft.block.HopperBlock.class,
    net.minecraft.block.JigsawBlock.class,
    net.minecraft.block.KelpBlock.class,
    net.minecraft.block.LadderBlock.class,
    net.minecraft.block.LanternBlock.class,
    net.minecraft.block.LeafLitterBlock.class,
    net.minecraft.block.LeavesBlock.class,
    net.minecraft.block.LecternBlock.class,
    net.minecraft.block.LightningRodBlock.class,
    net.minecraft.block.LoomBlock.class,
    net.minecraft.block.MangroveRootsBlock.class,
    net.minecraft.block.MultifaceBlock.class,
    net.minecraft.block.MushroomBlock.class,
    net.minecraft.block.NoteBlock.class,
    net.minecraft.block.ObserverBlock.class,
    net.minecraft.block.PaleMossCarpetBlock.class,
    net.minecraft.block.PaneBlock.class,
    net.minecraft.block.PillarBlock.class,
    net.minecraft.block.PistonBlock.class,
    net.minecraft.block.PitcherCropBlock.class,
    net.minecraft.block.PotentSulfurBlock.class,
    net.minecraft.block.PropaguleBlock.class,
    net.minecraft.block.RedstoneLampBlock.class,
    net.minecraft.block.RedstoneWireBlock.class,
    net.minecraft.block.RepeaterBlock.class,
    net.minecraft.block.RotatedInfestedBlock.class,
    net.minecraft.block.ScaffoldingBlock.class,
    net.minecraft.block.SculkSensorBlock.class,
    net.minecraft.block.SculkShriekerBlock.class,
    net.minecraft.block.SeagrassBlock.class,
    net.minecraft.block.SeaPickleBlock.class,
    net.minecraft.block.ShelfBlock.class,
    net.minecraft.block.ShelfMushroomBlock.class,
    net.minecraft.block.ShulkerBoxBlock.class,
    net.minecraft.block.SignBlock.class,
    net.minecraft.block.SkullBlock.class,
    net.minecraft.block.SlabBlock.class,
    net.minecraft.block.SmallDripleafBlock.class,
    net.minecraft.block.SnowBlock.class,
    net.minecraft.block.SnowyBlock.class,
    net.minecraft.block.SpeleothemBlock.class,
    net.minecraft.block.StairsBlock.class,
    net.minecraft.block.StonecutterBlock.class,
    net.minecraft.block.TallPlantBlock.class,
    net.minecraft.block.TallSeagrassBlock.class,
    net.minecraft.block.TestBlock.class,
    net.minecraft.block.TrapdoorBlock.class,
    net.minecraft.block.TripwireBlock.class,
    net.minecraft.block.TripwireHookBlock.class,
    net.minecraft.block.TurtleEggBlock.class,
    net.minecraft.block.VaultBlock.class,
    net.minecraft.block.VineBlock.class,
    net.minecraft.block.WallBannerBlock.class,
    net.minecraft.block.WallBlock.class,
    net.minecraft.block.WallHangingSignBlock.class,
    net.minecraft.block.WallMountedBlock.class,
    net.minecraft.block.WallRedstoneTorchBlock.class,
    net.minecraft.block.WallSignBlock.class,
    net.minecraft.block.WallSkullBlock.class,
    net.minecraft.block.WallTorchBlock.class
})
public abstract class MultipleMixins2 {
    @ModifyReturnValue(
        method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;",
        at = @At("RETURN")
    )
    private @Nullable BlockState modifyPlacementState(
        @Nullable BlockState original, final ItemPlacementContext ctx
    ) {
        if (original != null) {
            Block block = original.getBlock();
            StringProperty fluidlogged =
            ModifyExisting.OVERRIDES.get(block.getClass());
            if (fluidlogged != null) {
                FluidState replacedFluidState =
                ctx.getWorld().getFluidState(ctx.getBlockPos());
                if (block instanceof SlabBlock &&
                    SlabType.DOUBLE == original.getOrEmpty(
                        SlabBlock.TYPE
                    ).orElse(SlabType.BOTTOM)
                ) {
                    return replacedFluidState.getBlockState().isReplaceable() ?
                           original.with(
                        fluidlogged, fluidlogged.getValues().get(0)
                    ) : null;
                }
                String escapedState = FluidStateTranscript.escape(
                    this instanceof Waterloggable &&
                    fluidlogged.getName().equals(WATERLOGGED.getName()) ?
                    FluidStateTranscript
                    .transcriptWaterlogged(replacedFluidState) :
                    FluidStateTranscript.transcript(replacedFluidState)
                );
                if (fluidlogged.parse(escapedState).isPresent()) {
                    return original.with(fluidlogged, escapedState);
                }
            }
        }
        return original;
    }
}
