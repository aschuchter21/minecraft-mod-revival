/*
 * Compile-only probe for Galacticraft 5.0.0-prealpha CoalGeneratorBlockEntity.
 * This mirrors the MachineLib-facing calls made by the real 1.20.1 source.
 */
package dev.galacticraft.machinelib.forge.compat;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Not loaded by the mod. If this compiles, MachineLib exposes the exact runtime
 * surface used by Galacticraft's Coal Generator after removing Fabric transactions.
 */
abstract class CoalGeneratorRuntimeProbe extends MachineBlockEntity {
    private final SlotGroupType energyToItem;
    private final SlotGroupType coal;

    protected CoalGeneratorRuntimeProbe(
            MachineType<? extends MachineBlockEntity, ? extends AbstractContainerMenu> type,
            BlockPos pos,
            BlockState state,
            SlotGroupType energyToItem,
            SlotGroupType coal) {
        super(type, pos, state);
        this.energyToItem = energyToItem;
        this.coal = coal;
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(level, pos, state, profiler);
        this.drainPowerToStack(this.energyToItem);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.energyStorage().insert(120);
        this.trySpreadEnergy(level, state);

        ItemResourceSlot fuel = this.itemStorage().getGroup(this.coal).getSlot(0);
        if (!fuel.isEmpty()) fuel.extractOne();

        MachineStatus status = this.energyStorage().isFull() ? MachineStatuses.CAPACITOR_FULL : MachineStatuses.ACTIVE;
        this.setStatus(status);
        if (status.type().isActive()) this.setChanged();
        return status;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return null;
    }
}
