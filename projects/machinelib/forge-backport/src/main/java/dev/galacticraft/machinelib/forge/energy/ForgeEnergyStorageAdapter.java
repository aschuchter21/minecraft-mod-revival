/*
 * Copyright (c) 2021-2026 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */
package dev.galacticraft.machinelib.forge.energy;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Forge Energy view over MachineLib's long-valued storage.
 *
 * Forge 1.20.1 exposes energy amounts as ints. Values are saturated at
 * Integer.MAX_VALUE at this boundary only; the backing MachineLib storage
 * remains long-valued.
 */
public final class ForgeEnergyStorageAdapter implements IEnergyStorage {
    private final MachineEnergyStorage storage;
    private final long maxReceive;
    private final long maxExtract;

    public ForgeEnergyStorageAdapter(MachineEnergyStorage storage, long maxReceive, long maxExtract) {
        if (storage == null) {
            throw new IllegalArgumentException("storage cannot be null");
        }
        this.storage = storage;
        this.maxReceive = Math.max(0, maxReceive);
        this.maxExtract = Math.max(0, maxExtract);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0 || !this.canReceive()) return 0;

        long requested = Math.min((long) maxReceive, this.maxReceive);
        long accepted = this.storage.tryInsert(requested);
        if (!simulate && accepted > 0) {
            accepted = this.storage.insert(accepted);
        }
        return saturatingInt(accepted);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (maxExtract <= 0 || !this.canExtract()) return 0;

        long requested = Math.min((long) maxExtract, this.maxExtract);
        long available = this.storage.tryExtract(requested);
        if (!simulate && available > 0) {
            available = this.storage.extract(available);
        }
        return saturatingInt(available);
    }

    @Override
    public int getEnergyStored() {
        return saturatingInt(this.storage.getAmount());
    }

    @Override
    public int getMaxEnergyStored() {
        return saturatingInt(this.storage.getCapacity());
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0 && this.storage.canExposedExtract();
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0 && this.storage.canExposedInsert();
    }

    private static int saturatingInt(long value) {
        if (value <= 0) return 0;
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
