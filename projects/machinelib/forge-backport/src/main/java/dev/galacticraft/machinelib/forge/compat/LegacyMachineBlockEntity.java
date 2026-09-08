/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.forge.compat;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Transitional source-compatibility alias retained for the first Forge machine
 * overlays. The old flat-slot helpers now live on MachineBlockEntity itself,
 * matching MachineLib 0.3 and allowing recipe-machine subclasses to use them.
 */
public abstract class LegacyMachineBlockEntity extends MachineBlockEntity {
    protected LegacyMachineBlockEntity(
            @NotNull MachineType<? extends MachineBlockEntity, ? extends AbstractContainerMenu> type,
            @NotNull BlockPos pos,
            @NotNull BlockState state) {
        super(type, pos, state);
    }
}
