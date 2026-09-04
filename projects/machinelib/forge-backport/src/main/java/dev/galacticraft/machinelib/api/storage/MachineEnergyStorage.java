/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.MachineEnergyStorageImpl;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * MachineLib's long-valued internal energy store with a native Forge Energy view.
 * Includes the 0.2 `of(...)` factories used by Galacticraft 1.20.1.
 */
public interface MachineEnergyStorage {
    static MachineEnergyStorage create(long capacity, long maxInput, long maxOutput, boolean insert, boolean extract) {
        return new MachineEnergyStorageImpl(capacity, maxInput, maxOutput, insert, extract);
    }

    static MachineEnergyStorage of(long capacity, long insertion, long extraction,
                                   boolean externalInsertion, boolean externalExtraction) {
        if (capacity == 0 || insertion == 0 || extraction == 0) return empty();
        if (capacity < 0 || insertion < 0 || extraction < 0) throw new IllegalArgumentException("negative energy value");
        return create(capacity, insertion, extraction, externalInsertion, externalExtraction);
    }

    static MachineEnergyStorage of(long capacity, long ioRate,
                                   boolean externalInsertion, boolean externalExtraction) {
        return of(capacity, ioRate, ioRate, externalInsertion, externalExtraction);
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

    default LongTag createTag() { return LongTag.valueOf(getAmount()); }
    default void readTag(LongTag tag) { setEnergy(tag.getAsLong()); }
    default void writePacket(FriendlyByteBuf buf) { buf.writeLong(getAmount()); }
    default void readPacket(FriendlyByteBuf buf) { setEnergy(buf.readLong()); }
    default long getModifications() { return getAmount(); }
}
