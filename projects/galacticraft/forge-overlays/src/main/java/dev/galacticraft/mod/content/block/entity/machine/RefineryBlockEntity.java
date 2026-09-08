/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.machine;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.mod.Galacticraft;
import dev.galacticraft.mod.content.GCFluids;
import dev.galacticraft.mod.content.GCMachineTypes;
import dev.galacticraft.mod.machine.GCMachineStatuses;
import dev.galacticraft.mod.util.FluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/** Forge 1.20.1 overlay for Galacticraft's refinery. */
public class RefineryBlockEntity extends MachineBlockEntity {
    public static final int CHARGE_SLOT = 0;
    public static final int OIL_INPUT_SLOT = 1;
    public static final int FUEL_OUTPUT_SLOT = 2;
    public static final int OIL_TANK = 0;
    public static final int FUEL_TANK = 1;

    @VisibleForTesting
    public static final long MAX_CAPACITY = FluidUtil.bucketsToDroplets(8);

    private static final long REFINING_UNITS_PER_TICK = FluidResourceSlot.INTERNAL_UNITS_PER_BUCKET / 100L;

    public RefineryBlockEntity(BlockPos pos, BlockState state) {
        super(GCMachineTypes.REFINERY, pos, state);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        this.chargeFromStack(CHARGE_SLOT);
        this.moveFluidFromItem(OIL_INPUT_SLOT, OIL_TANK, GCFluids.CRUDE_OIL);
        this.moveFluidToItem(FUEL_TANK, FUEL_OUTPUT_SLOT, GCFluids.FUEL);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        FluidResourceSlot oilTank = this.fluidStorage().getSlot(OIL_TANK);
        if (oilTank.isEmpty()) return GCMachineStatuses.MISSING_OIL;

        FluidResourceSlot fuelTank = this.fluidStorage().getSlot(FUEL_TANK);
        if (fuelTank.isFull()) return GCMachineStatuses.FUEL_TANK_FULL;

        profiler.push("refining");
        try {
            long energy = Galacticraft.CONFIG_MANAGER.get().refineryEnergyConsumptionRate();
            if (!this.energyStorage().canExtract(energy)) {
                return MachineStatuses.NOT_ENOUGH_ENERGY;
            }

            long space = fuelTank.tryInsert(GCFluids.FUEL, REFINING_UNITS_PER_TICK);
            if (space > 0) {
                long oil = oilTank.tryExtract(GCFluids.CRUDE_OIL, space);
                if (oil > 0) {
                    this.energyStorage().extract(energy);
                    oilTank.extract(oil);
                    fuelTank.insert(GCFluids.FUEL, oil);
                }
            }
            return MachineStatuses.ACTIVE;
        } finally {
            profiler.pop();
        }
    }

    /** Drain the requested Forge fluid from an inventory container into an internal MachineLib tank. */
    private void moveFluidFromItem(int itemIndex, int tankIndex, @NotNull Fluid fluid) {
        FluidResourceSlot target = this.fluidStorage().getSlot(tankIndex);
        if (target.isFull()) return;

        ItemResourceSlot itemSlot = this.itemStorage().getSlot(itemIndex);
        ItemStack stack = itemSlot.toStack();
        if (stack.isEmpty()) return;

        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null) return;

        long roomUnits = target.tryInsert(fluid, Long.MAX_VALUE);
        int roomMb = (int) Math.min(Integer.MAX_VALUE,
                roomUnits / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        if (roomMb <= 0) return;

        FluidStack simulated = handler.drain(new FluidStack(fluid, roomMb), IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty()) return;

        FluidStack drained = handler.drain(new FluidStack(fluid, simulated.getAmount()), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return;

        target.insert(fluid, (long) drained.getAmount() * FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        itemSlot.setStack(handler.getContainer());
    }

    /** Fill a Forge inventory fluid container from an internal MachineLib tank. */
    private void moveFluidToItem(int tankIndex, int itemIndex, @NotNull Fluid fluid) {
        FluidResourceSlot source = this.fluidStorage().getSlot(tankIndex);
        if (source.isEmpty() || !source.contains(fluid)) return;

        ItemResourceSlot itemSlot = this.itemStorage().getSlot(itemIndex);
        ItemStack stack = itemSlot.toStack();
        if (stack.isEmpty()) return;

        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null) return;

        int availableMb = (int) Math.min(Integer.MAX_VALUE,
                source.getAmount() / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        if (availableMb <= 0) return;

        FluidStack offered = new FluidStack(fluid, availableMb);
        int accepted = handler.fill(offered, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;

        int inserted = handler.fill(new FluidStack(fluid, accepted), IFluidHandler.FluidAction.EXECUTE);
        if (inserted <= 0) return;

        source.extract((long) inserted * FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        itemSlot.setStack(handler.getContainer());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        if (this.getSecurity().hasAccess(player)) {
            return new MachineMenu<>(syncId, (ServerPlayer) player, this);
        }
        return null;
    }
}
