/* Compile-only probe for Galacticraft 5.0.0-prealpha CoalGeneratorBlockEntity. */
package dev.galacticraft.machinelib.forge.compat;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
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
 * Not loaded by the mod. If this compiles, the Forge compatibility layer exposes
 * the same flat-slot machine surface used by Galacticraft 1.20.1's real coal
 * generator after removing Fabric transactions.
 */
abstract class CoalGeneratorRuntimeProbe extends LegacyMachineBlockEntity {
    private static final int CHARGE_SLOT = 0;
    private static final int INPUT_SLOT = 1;

    protected CoalGeneratorRuntimeProbe(
            MachineType<? extends MachineBlockEntity, ? extends AbstractContainerMenu> type,
            BlockPos pos,
            BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(level, pos, state, profiler);
        this.drainPowerToStack(CHARGE_SLOT);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.energyStorage().insert(120);
        this.trySpreadEnergy(level, state);

        ItemResourceSlot fuel = this.itemStorage().getSlot(INPUT_SLOT);
        if (!fuel.isEmpty()) fuel.consumeOne();

        MachineStatus status = this.energyStorage().isFull()
                ? MachineStatuses.CAPACITOR_FULL
                : MachineStatuses.ACTIVE;
        this.setStatus(status);
        if (status.type().isActive()) this.setChanged();
        return status;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return null;
    }
}
