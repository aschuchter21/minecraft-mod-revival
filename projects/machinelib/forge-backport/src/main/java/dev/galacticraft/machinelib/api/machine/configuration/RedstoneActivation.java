/* Copyright (c) 2021-2023 Team Galacticraft - MIT License */
package dev.galacticraft.machinelib.api.machine.configuration;

import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

/** Historical machine redstone control modes. */
public enum RedstoneActivation {
    LOW,
    HIGH,
    IGNORE;

    public boolean isDisabled(Level level, BlockPos pos) {
        return switch (this) {
            case LOW -> level.hasNeighborSignal(pos);
            case HIGH -> !level.hasNeighborSignal(pos);
            case IGNORE -> false;
        };
    }
}