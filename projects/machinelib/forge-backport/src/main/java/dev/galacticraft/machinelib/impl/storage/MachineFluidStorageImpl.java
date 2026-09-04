/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.forge.fluid.ForgeFluidStorageAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

public final class MachineFluidStorageImpl implements MachineFluidStorage {
    public static final MachineFluidStorageImpl EMPTY = new MachineFluidStorageImpl(new FluidResourceSlot[0]);

    private final FluidResourceSlot[] slots;
    private final Map<SlotGroupType, SlotGroup<Fluid, FluidStack, FluidResourceSlot>> groups = new IdentityHashMap<>();
    private Runnable listener;

    public MachineFluidStorageImpl(FluidResourceSlot[] slots) {
        this.slots = slots.clone();
        for (FluidResourceSlot slot : this.slots) slot.setListener(this::markModified);
    }

    public MachineFluidStorageImpl(SlotGroupType[] types, SlotGroup<Fluid, FluidStack, FluidResourceSlot>[] groups) {
        if (types.length != groups.length) throw new IllegalArgumentException("type/group length mismatch");
        int total = 0;
        for (SlotGroup<Fluid, FluidStack, FluidResourceSlot> group : groups) total += group.size();
        this.slots = new FluidResourceSlot[total];
        int offset = 0;
        for (int i = 0; i < groups.length; i++) {
            SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = groups[i];
            this.groups.put(types[i], group);
            for (FluidResourceSlot slot : group) {
                slot.assignInputType(types[i].inputType());
                slot.setListener(this::markModified);
                this.slots[offset++] = slot;
            }
        }
    }

    @Override public int size() { return this.slots.length; }
    @Override public FluidResourceSlot slot(int slot) { return this.slots[slot]; }
    @Override public FluidResourceSlot[] getSlots() { return this.slots.clone(); }
    @Override public ResourceFilter<Fluid> getStrictFilter(int slot) { return this.slots[slot].getStrictFilter(); }
    @Override public SlotGroup<Fluid, FluidStack, FluidResourceSlot> getGroup(SlotGroupType type) {
        SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = this.groups.get(type);
        if (group == null) throw new IllegalArgumentException("Unknown slot group: " + type.name().getString());
        return group;
    }
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

    private void markModified() { if (this.listener != null) this.listener.run(); }
}
