/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine;

import net.minecraft.ChatFormatting;

public final class MachineStatuses {
    public static final MachineStatus NOT_ENOUGH_ENERGY = MachineStatus.create("status.machinelib.not_enough_energy", ChatFormatting.RED, MachineStatus.Type.MISSING_ENERGY);
    public static final MachineStatus INVALID_RECIPE = MachineStatus.create("status.machinelib.invalid_recipe", ChatFormatting.RED, MachineStatus.Type.MISSING_ITEMS);
    public static final MachineStatus OUTPUT_FULL = MachineStatus.create("status.machinelib.output_full", ChatFormatting.GOLD, MachineStatus.Type.OUTPUT_FULL);
    public static final MachineStatus CAPACITOR_FULL = MachineStatus.create("status.machinelib.capacitor_full", ChatFormatting.GOLD, MachineStatus.Type.OUTPUT_FULL);
    public static final MachineStatus IDLE = MachineStatus.create("status.machinelib.idle", ChatFormatting.GOLD, MachineStatus.Type.MISSING_RESOURCE);
    public static final MachineStatus ACTIVE = MachineStatus.create("status.machinelib.active", ChatFormatting.GREEN, MachineStatus.Type.WORKING);
    public static final MachineStatus OFF = MachineStatus.create("status.machinelib.off", ChatFormatting.RED, MachineStatus.Type.OTHER);
    private MachineStatuses() {}
}
