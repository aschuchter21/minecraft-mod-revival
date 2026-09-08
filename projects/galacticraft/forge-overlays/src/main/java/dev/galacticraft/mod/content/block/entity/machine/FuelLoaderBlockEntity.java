/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.machine;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.mod.content.GCBlocks;
import dev.galacticraft.mod.content.GCFluids;
import dev.galacticraft.mod.content.GCMachineTypes;
import dev.galacticraft.mod.content.block.special.rocketlaunchpad.RocketLaunchPadBlock;
import dev.galacticraft.mod.content.block.special.rocketlaunchpad.RocketLaunchPadBlockEntity;
import dev.galacticraft.mod.content.entity.RocketEntity;
import dev.galacticraft.mod.machine.GCMachineStatuses;
import dev.galacticraft.mod.screen.FuelLoaderMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 overlay for Galacticraft's rocket fuel loader. */
public class FuelLoaderBlockEntity extends MachineBlockEntity {
    public static final int CHARGE_SLOT = 0;
    public static final int FUEL_INPUT_SLOT = 1;
    public static final int FUEL_TANK = 0;
    private static final int TRANSFER_MB_PER_TICK = 20;

    private BlockPos connectionPos = BlockPos.ZERO;
    private Direction check;

    public FuelLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(GCMachineTypes.FUEL_LOADER, pos, state);
    }

    @NotNull
    public BlockPos getConnectionPos() {
        return this.connectionPos;
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        if (BlockPos.ZERO.equals(this.connectionPos)) return GCMachineStatuses.NO_ROCKET;

        BlockEntity blockEntity = level.getBlockEntity(this.connectionPos);
        Entity entity;
        if (blockEntity instanceof RocketLaunchPadBlockEntity launchPad) {
            if (!launchPad.hasRocket()) return GCMachineStatuses.NO_ROCKET;
            entity = level.getEntity(launchPad.getRocketEntityId());
            if (!(entity instanceof RocketEntity rocket)) return GCMachineStatuses.NO_ROCKET;

            FluidResourceSlot source = this.fluidStorage().getSlot(FUEL_TANK);
            if (source.isEmpty() || !source.contains(GCFluids.FUEL)) return GCMachineStatuses.NOT_ENOUGH_FUEL;

            IFluidHandler rocketTank = rocket.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            if (rocketTank == null) return GCMachineStatuses.NO_ROCKET;

            int availableMb = (int) Math.min(TRANSFER_MB_PER_TICK,
                    source.getAmount() / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
            if (availableMb <= 0) return GCMachineStatuses.NOT_ENOUGH_FUEL;

            FluidStack offered = new FluidStack(GCFluids.FUEL, availableMb);
            int accepted = rocketTank.fill(offered, IFluidHandler.FluidAction.SIMULATE);
            if (accepted <= 0) return GCMachineStatuses.ROCKET_IS_FULL;

            int inserted = rocketTank.fill(new FluidStack(GCFluids.FUEL, accepted), IFluidHandler.FluidAction.EXECUTE);
            if (inserted <= 0) return GCMachineStatuses.ROCKET_IS_FULL;

            source.extract((long) inserted * FluidResourceSlot.INTERNAL_UNITS_PER_MB);
            return GCMachineStatuses.LOADING;
        }
        return GCMachineStatuses.NO_ROCKET;
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(level, pos, state, profiler);

        if (this.check != null) {
            BlockPos launchPad = this.worldPosition.relative(this.check);
            if (level.getBlockState(launchPad).getBlock() == GCBlocks.ROCKET_LAUNCH_PAD) {
                launchPad = launchPad.offset(RocketLaunchPadBlock.partToCenterPos(
                        level.getBlockState(launchPad).getValue(RocketLaunchPadBlock.PART)));
                if (level.getBlockState(launchPad).getBlock() instanceof RocketLaunchPadBlock
                        && level.getBlockState(launchPad).getValue(RocketLaunchPadBlock.PART) == RocketLaunchPadBlock.Part.CENTER
                        && level.getBlockEntity(launchPad) instanceof RocketLaunchPadBlockEntity) {
                    this.connectionPos = launchPad;
                }
            }
            this.check = null;
        }

        this.chargeFromStack(CHARGE_SLOT);
        this.takeFuelFromStack();
    }

    /** Move Forge item-fluid contents into MachineLib's internal-unit fuel tank. */
    private void takeFuelFromStack() {
        FluidResourceSlot target = this.fluidStorage().getSlot(FUEL_TANK);
        if (target.isFull()) return;

        ItemResourceSlot itemSlot = this.itemStorage().getSlot(FUEL_INPUT_SLOT);
        ItemStack stack = itemSlot.toStack();
        if (stack.isEmpty()) return;

        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null) return;

        long roomUnits = target.tryInsert(GCFluids.FUEL, Long.MAX_VALUE);
        int roomMb = (int) Math.min(Integer.MAX_VALUE,
                roomUnits / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        if (roomMb <= 0) return;

        FluidStack available = handler.drain(new FluidStack(GCFluids.FUEL, roomMb), IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty()) return;

        FluidStack drained = handler.drain(new FluidStack(GCFluids.FUEL, available.getAmount()),
                IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return;

        target.insert(GCFluids.FUEL,
                (long) drained.getAmount() * FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        itemSlot.setStack(handler.getContainer());
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        if (!BlockPos.ZERO.equals(this.connectionPos)) {
            tag.putBoolean("has_connection", true);
            tag.putLong("connection_pos", this.connectionPos.asLong());
        }
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.connectionPos = tag.getBoolean("has_connection")
                ? BlockPos.of(tag.getLong("connection_pos"))
                : BlockPos.ZERO;
    }

    public void updateConnections(Direction direction) {
        this.check = direction;
    }

    public void setConnectionPos(@NotNull BlockPos connectionPos) {
        this.connectionPos = connectionPos;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new FuelLoaderMenu(syncId, (ServerPlayer) player, this);
    }
}
