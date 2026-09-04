/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.forge.fluid.ForgeFluidStorageAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Arrays;
import java.util.Iterator;

public final class MachineFluidStorageImpl implements MachineFluidStorage {
    public static final MachineFluidStorageImpl EMPTY = new MachineFluidStorageImpl(new FluidResourceSlot[0]);

    private final FluidResourceSlot[] slots;
    private Runnable listener;

    public MachineFluidStorageImpl(FluidResourceSlot[] slots) {
        this.slots = slots.clone();
        for (FluidResourceSlot slot : this.slots) slot.setListener(this::markModified);
    }

    @Override public int size() { return this.slots.length; }
    @Override public FluidResourceSlot slot(int slot) { return this.slots[slot]; }
    @Override public FluidResourceSlot[] getSlots() { return this.slots.clone(); }
    @Override public ResourceFilter<Fluid> getStrictFilter(int slot) { return this.slots[slot].getFilter(); }
    @Override public void setListener(Runnable listener) { this.listener = listener; }
    @Override public IFluidHandler getExposedStorage(ResourceFlow flow) { return new ForgeFluidStorageAdapter(this, flow); }
    @Override public Iterator<FluidResourceSlot> iterator() { return Arrays.asList(this.slots).iterator(); }

    @Override public boolean isEmpty() {
        for (FluidResourceSlot slot : this.slots) if (!slot.isEmpty()) return false;
        return true;
    }

    @Override public long insert(Fluid fluid, long amount) { return insert(fluid, null, amount); }
    @Override public long insert(Fluid fluid, CompoundTag tag, long amount) {
        long remaining = Math.max(0, amount);
        long inserted = 0;
        for (FluidResourceSlot slot : this.slots) {
            if (remaining == 0) break;
            long accepted = slot.tryInsert(fluid, tag, remaining);
            if (accepted > 0) {
                inserted += slot.insert(fluid, tag, accepted);
                remaining -= accepted;
            }
        }
        return inserted;
    }

    @Override public long extract(Fluid fluid, long amount) { return extract(fluid, null, amount); }
    @Override public long extract(Fluid fluid, CompoundTag tag, long amount) {
        long remaining = Math.max(0, amount);
        long extracted = 0;
        for (FluidResourceSlot slot : this.slots) {
            if (remaining == 0) break;
            long available = tag == null ? slot.tryExtract(fluid, remaining) : slot.tryExtract(fluid, tag, remaining);
            if (available > 0) {
                extracted += slot.extract(available);
                remaining -= available;
            }
        }
        return extracted;
    }

    private void markModified() {
        if (this.listener != null) this.listener.run();
    }
}
