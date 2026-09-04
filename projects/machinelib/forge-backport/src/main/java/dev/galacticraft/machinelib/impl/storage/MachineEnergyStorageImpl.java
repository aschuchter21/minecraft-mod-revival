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
package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.forge.energy.ForgeEnergyStorageAdapter;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Loader-neutral long-valued machine energy store for the Forge 1.20.1 backport.
 * Galacticraft continues to use long values internally; Forge int limits are
 * applied only by {@link ForgeEnergyStorageAdapter}.
 */
public final class MachineEnergyStorageImpl implements MachineEnergyStorage {
    private final long capacity;
    private final long maxInput;
    private final long maxOutput;
    private final boolean insert;
    private final boolean extract;

    private long amount;
    private Runnable listener;

    public MachineEnergyStorageImpl(long capacity, long maxInput, long maxOutput, boolean insert, boolean extract) {
        if (capacity < 0 || maxInput < 0 || maxOutput < 0) {
            throw new IllegalArgumentException("Energy capacity and transfer rates must be non-negative");
        }
        this.capacity = capacity;
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.insert = insert;
        this.extract = extract;
    }

    @Override
    public boolean canExtract(long amount) {
        return amount >= 0 && this.amount >= amount;
    }

    @Override
    public boolean canInsert(long amount) {
        return amount >= 0 && amount <= this.capacity - this.amount;
    }

    @Override
    public long tryExtract(long amount) {
        if (amount <= 0) return 0;
        return Math.min(this.amount, amount);
    }

    @Override
    public long tryInsert(long amount) {
        if (amount <= 0) return 0;
        return Math.min(amount, this.capacity - this.amount);
    }

    @Override
    public long extract(long amount) {
        long extracted = this.tryExtract(amount);
        if (extracted > 0) {
            this.amount -= extracted;
            this.markModified();
        }
        return extracted;
    }

    @Override
    public long insert(long amount) {
        long inserted = this.tryInsert(amount);
        if (inserted > 0) {
            this.amount += inserted;
            this.markModified();
        }
        return inserted;
    }

    @Override
    public boolean extractExact(long amount) {
        if (!this.canExtract(amount)) return false;
        if (amount > 0) {
            this.amount -= amount;
            this.markModified();
        }
        return true;
    }

    @Override
    public boolean insertExact(long amount) {
        if (!this.canInsert(amount)) return false;
        if (amount > 0) {
            this.amount += amount;
            this.markModified();
        }
        return true;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    @Override
    public boolean isFull() {
        return this.amount >= this.capacity;
    }

    @Override
    public boolean isEmpty() {
        return this.amount == 0;
    }

    @Override
    public void setEnergy(long amount) {
        long clamped = Math.max(0, Math.min(amount, this.capacity));
        if (this.amount != clamped) {
            this.amount = clamped;
            this.markModified();
        }
    }

    @Override
    public long externalInsertionRate() {
        return this.maxInput;
    }

    @Override
    public long externalExtractionRate() {
        return this.maxOutput;
    }

    @Override
    public boolean canExposedInsert() {
        return this.insert && this.maxInput > 0;
    }

    @Override
    public boolean canExposedExtract() {
        return this.extract && this.maxOutput > 0;
    }

    @Override
    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public IEnergyStorage getExposedStorage(ResourceFlow flow) {
        if (flow == null) return null;

        return switch (flow) {
            case INPUT -> this.canExposedInsert()
                    ? new ForgeEnergyStorageAdapter(this, this.maxInput, 0)
                    : null;
            case OUTPUT -> this.canExposedExtract()
                    ? new ForgeEnergyStorageAdapter(this, 0, this.maxOutput)
                    : null;
            case BOTH -> {
                long receive = this.canExposedInsert() ? this.maxInput : 0;
                long extract = this.canExposedExtract() ? this.maxOutput : 0;
                yield receive > 0 || extract > 0
                        ? new ForgeEnergyStorageAdapter(this, receive, extract)
                        : null;
            }
        };
    }

    private void markModified() {
        if (this.listener != null) {
            this.listener.run();
        }
    }
}
