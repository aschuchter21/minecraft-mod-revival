/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.machine;

import dev.galacticraft.api.gas.Gases;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.forge.compat.LegacyMachineBlockEntity;
import dev.galacticraft.mod.Galacticraft;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 overlay for Galacticraft's oxygen compressor. */
public class OxygenCompressorBlockEntity extends LegacyMachineBlockEntity {
    public static final int CHARGE_SLOT = 0;
    public static final int OXYGEN_OUTPUT_SLOT = 1;
    public static final int OXYGEN_TANK = 0;
    public static final long MAX_OXYGEN = FluidUtil.bucketsToDroplets(50);

    public OxygenCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(GCMachineTypes.OXYGEN_COMPRESSOR, pos, state);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        this.chargeFromStack(CHARGE_SLOT);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        FluidResourceSlot oxygenStorage = this.fluidStorage().getSlot(OXYGEN_TANK);
        if (oxygenStorage.isEmpty()) return GCMachineStatuses.NOT_ENOUGH_OXYGEN;

        profiler.push("find_storage");
        ItemResourceSlot outputSlot = this.itemStorage().getSlot(OXYGEN_OUTPUT_SLOT);
        ItemStack stack = outputSlot.toStack();
        IFluidHandlerItem tank = stack.isEmpty()
                ? null
                : stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        profiler.pop();
        if (tank == null) return GCMachineStatuses.MISSING_OXYGEN_TANK;

        int spaceMb = tank.fill(new FluidStack(Gases.OXYGEN, Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE);
        if (spaceMb <= 0) return GCMachineStatuses.OXYGEN_TANK_FULL;

        profiler.push("transfer");
        try {
            long energy = Galacticraft.CONFIG_MANAGER.get().oxygenCompressorEnergyConsumptionRate();
            if (!this.energyStorage().canExtract(energy)) return MachineStatuses.NOT_ENOUGH_ENERGY;

            long requestedUnits = (long) spaceMb * FluidResourceSlot.INTERNAL_UNITS_PER_MB;
            long availableUnits = oxygenStorage.tryExtract(Gases.OXYGEN, requestedUnits);
            int transferMb = (int) Math.min(Integer.MAX_VALUE,
                    availableUnits / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
            if (transferMb > 0) {
                int acceptedMb = tank.fill(new FluidStack(Gases.OXYGEN, transferMb), IFluidHandler.FluidAction.EXECUTE);
                if (acceptedMb > 0) {
                    oxygenStorage.extract((long) acceptedMb * FluidResourceSlot.INTERNAL_UNITS_PER_MB);
                    this.energyStorage().extract(energy);
                    outputSlot.setStack(tank.getContainer());
                }
            }
        } finally {
            profiler.pop();
        }
        return GCMachineStatuses.COMPRESSING;
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
