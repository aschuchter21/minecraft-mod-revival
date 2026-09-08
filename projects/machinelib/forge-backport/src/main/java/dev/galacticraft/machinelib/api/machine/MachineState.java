/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.impl.machine.MachineStateImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Loader-neutral subset of MachineLib 0.3 machine state used by Galacticraft 1.20.1. */
public interface MachineState {
    static @NotNull MachineState create(@NotNull MachineType<?, ?> type) {
        return new MachineStateImpl(type);
    }

    @Nullable MachineStatus getStatus();
    void setStatus(@Nullable MachineStatus status);
    boolean isActive();
    boolean isPowered();
    void setPowered(boolean powered);
}
