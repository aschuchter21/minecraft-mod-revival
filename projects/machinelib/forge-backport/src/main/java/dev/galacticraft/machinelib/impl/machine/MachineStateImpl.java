/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge/runtime implementation of the MachineLib 0.3 state contract. */
public final class MachineStateImpl implements MachineState {
    @SuppressWarnings("unused")
    private final MachineType<?, ?> type;
    private MachineStatus status = MachineStatus.INVALID;
    private boolean powered;

    public MachineStateImpl(@NotNull MachineType<?, ?> type) {
        this.type = type;
    }

    @Override public @Nullable MachineStatus getStatus() { return this.status; }
    @Override public void setStatus(@Nullable MachineStatus status) { this.status = status; }
    @Override public boolean isActive() { return this.status != null && this.status.type().isActive(); }
    @Override public boolean isPowered() { return this.powered; }
    @Override public void setPowered(boolean powered) { this.powered = powered; }
}
