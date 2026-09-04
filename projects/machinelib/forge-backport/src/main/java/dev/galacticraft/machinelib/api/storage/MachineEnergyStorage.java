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
package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.MachineEnergyStorageImpl;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * MachineLib's long-valued internal energy store with a native Forge Energy view.
 * Internal Galacticraft/MachineLib code remains long-based; Forge's int boundary
 * is handled only by the exposed capability adapter.
 */
public interface MachineEnergyStorage {
    static MachineEnergyStorage create(long capacity, long maxInput, long maxOutput, boolean insert, boolean extract) {
        return new MachineEnergyStorageImpl(capacity, maxInput, maxOutput, insert, extract);
    }

    static MachineEnergyStorage empty() {
        return create(0, 0, 0, false, false);
    }

    boolean canExtract(long amount);
    boolean canInsert(long amount);
    long tryExtract(long amount);
    long tryInsert(long amount);
    long extract(long amount);
    long insert(long amount);
    boolean extractExact(long amount);
    boolean insertExact(long amount);
    long getAmount();
    long getCapacity();
    boolean isFull();
    boolean isEmpty();
    void setEnergy(long amount);
    long externalInsertionRate();
    long externalExtractionRate();
    boolean canExposedInsert();
    boolean canExposedExtract();
    void setListener(Runnable listener);
    IEnergyStorage getExposedStorage(ResourceFlow flow);

    default LongTag createTag() {
        return LongTag.valueOf(getAmount());
    }

    default void readTag(LongTag tag) {
        setEnergy(tag.getAsLong());
    }

    default void writePacket(FriendlyByteBuf buf) {
        buf.writeLong(getAmount());
    }

    default void readPacket(FriendlyByteBuf buf) {
        setEnergy(buf.readLong());
    }

    default long getModifications() {
        return getAmount();
    }
}
