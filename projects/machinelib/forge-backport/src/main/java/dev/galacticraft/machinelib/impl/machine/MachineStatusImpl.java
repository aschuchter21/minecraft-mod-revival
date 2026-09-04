/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import net.minecraft.network.chat.Component;

public record MachineStatusImpl(Component text, MachineStatus.Type type) implements MachineStatus {
    @Override public Component getText() { return this.text; }
    @Override public MachineStatus.Type getType() { return this.type; }
}
