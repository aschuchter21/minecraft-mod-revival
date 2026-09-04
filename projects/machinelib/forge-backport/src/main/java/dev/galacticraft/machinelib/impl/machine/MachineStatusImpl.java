/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import net.minecraft.network.chat.Component;

public record MachineStatusImpl(Component name, MachineStatus.Type type) implements MachineStatus {
}
