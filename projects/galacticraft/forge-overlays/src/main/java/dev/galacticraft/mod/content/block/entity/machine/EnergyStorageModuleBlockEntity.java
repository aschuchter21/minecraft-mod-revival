/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.machine;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.forge.compat.LegacyMachineBlockEntity;
import dev.galacticraft.mod.content.GCMachineTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 overlay for Galacticraft's energy storage module. */
public class EnergyStorageModuleBlockEntity extends LegacyMachineBlockEntity {
    public static final int CHARGE_SELF_SLOT = 0;
    public static final int CHARGE_ITEM_SLOT = 1;

    public EnergyStorageModuleBlockEntity(BlockPos pos, BlockState state) {
        super(GCMachineTypes.ENERGY_STORAGE_MODULE, pos, state);
    }

    @Override
    public long getEnergyItemExtractionRate() {
        return super.getEnergyItemExtractionRate() * 2;
    }

    @Override
    public long getEnergyItemInsertionRate() {
        return super.getEnergyItemInsertionRate() * 2;
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        this.chargeFromStack(CHARGE_SELF_SLOT);
        this.drainPowerToStack(CHARGE_ITEM_SLOT);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.trySpreadEnergy(level, state);
        return MachineStatuses.ACTIVE;
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
