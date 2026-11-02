/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine.configuration;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Historical MachineLib redstone modes. Declaration order is ABI-sensitive:
 * original 0.3 serialized these values by ordinal as IGNORE, LOW, HIGH.
 */
public enum RedstoneActivation implements StringRepresentable {
    IGNORE(Component.translatable("ui.machinelib.machine.redstone_activation.ignore")),
    LOW(Component.translatable("ui.machinelib.machine.redstone_activation.low")),
    HIGH(Component.translatable("ui.machinelib.machine.redstone_activation.high"));

    public static final RedstoneActivation[] VALUES = values();
    private final Component name;

    RedstoneActivation(Component name) { this.name = name; }

    public boolean isActive(boolean powered) {
        return switch (this) {
            case IGNORE -> true;
            case LOW -> !powered;
            case HIGH -> powered;
        };
    }

    public boolean isDisabled(Level level, BlockPos pos) {
        return !isActive(level.hasNeighborSignal(pos));
    }

    public @NotNull Component getName() { return this.name; }

    @Override
    public @NotNull String getSerializedName() {
        return switch (this) {
            case IGNORE -> "ignore";
            case LOW -> "low";
            case HIGH -> "high";
        };
    }
}
