/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.MachineFluidStorageImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.function.Supplier;

public interface MachineFluidStorage extends Iterable<FluidResourceSlot> {
    static MachineFluidStorage create(FluidResourceSlot... slots) {
        return slots.length == 0 ? empty() : new MachineFluidStorageImpl(slots);
    }

    static Supplier<MachineFluidStorage> of(FluidResourceSlot.Builder... slots) {
        return () -> {
            FluidResourceSlot[] built = new FluidResourceSlot[slots.length];
            for (int i = 0; i < slots.length; i++) built[i] = slots[i].build();
            return create(built);
        };
    }

    static MachineFluidStorage empty() { return MachineFluidStorageImpl.EMPTY; }

    int size();
    FluidResourceSlot slot(int slot);
    default FluidResourceSlot getSlot(int slot) { return slot(slot); }
    FluidResourceSlot[] getSlots();
    ResourceFilter<Fluid> getStrictFilter(int slot);
    void setListener(Runnable listener);
    IFluidHandler getExposedStorage(ResourceFlow flow);

    boolean isEmpty();
    long insert(Fluid fluid, long amount);
    long insert(Fluid fluid, CompoundTag tag, long amount);
    long extract(Fluid fluid, long amount);
    long extract(Fluid fluid, CompoundTag tag, long amount);
}
