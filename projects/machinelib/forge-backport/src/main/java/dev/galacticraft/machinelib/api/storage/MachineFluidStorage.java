/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.MachineFluidStorageImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** MachineLib 0.2-compatible grouped fluid storage backed by Forge fluid capabilities. */
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

    static Builder builder() { return new Builder(); }
    static MachineFluidStorage empty() { return MachineFluidStorageImpl.EMPTY; }

    int size();
    FluidResourceSlot slot(int slot);
    default FluidResourceSlot getSlot(int slot) { return slot(slot); }
    FluidResourceSlot[] getSlots();
    ResourceFilter<Fluid> getStrictFilter(int slot);
    SlotGroup<Fluid, FluidStack, FluidResourceSlot> getGroup(SlotGroupType type);
    void setListener(Runnable listener);
    IFluidHandler getExposedStorage(ResourceFlow flow);
    boolean isEmpty();
    long insert(Fluid fluid, long amount);
    long insert(Fluid fluid, CompoundTag tag, long amount);
    long extract(Fluid fluid, long amount);
    long extract(Fluid fluid, CompoundTag tag, long amount);

    /** Persist historical internal fluid units, not Forge millibuckets. */
    default ListTag createTag() {
        ListTag list = new ListTag();
        FluidResourceSlot[] slots = getSlots();
        for (int i = 0; i < slots.length; i++) {
            FluidResourceSlot slot = slots[i];
            if (slot.isEmpty()) continue;
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(slot.getResource());
            if (id == null) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", i);
            entry.putString("Fluid", id.toString());
            entry.putLong("Amount", slot.getAmount());
            CompoundTag data = slot.copyTag();
            if (data != null && !data.isEmpty()) entry.put("Tag", data);
            list.add(entry);
        }
        return list;
    }

    default void readTag(ListTag list) {
        FluidResourceSlot[] slots = getSlots();
        for (FluidResourceSlot slot : slots) if (!slot.isEmpty()) slot.set(null, null, 0);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slotIndex = entry.getInt("Slot");
            if (slotIndex < 0 || slotIndex >= slots.length || !entry.contains("Fluid", Tag.TAG_STRING)) continue;
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("Fluid"));
            if (id == null || !BuiltInRegistries.FLUID.containsKey(id)) continue;
            Fluid fluid = BuiltInRegistries.FLUID.get(id);
            long amount = entry.getLong("Amount");
            CompoundTag data = entry.contains("Tag", Tag.TAG_COMPOUND) ? entry.getCompound("Tag") : null;
            slots[slotIndex].set(fluid, data, Math.min(amount, slots[slotIndex].getCapacity()));
        }
    }

    final class Builder implements Supplier<MachineFluidStorage> {
        private final List<SlotGroupType> types = new ArrayList<>();
        private final List<Supplier<SlotGroup<Fluid, FluidStack, FluidResourceSlot>>> groups = new ArrayList<>();

        private Builder() {}

        public Builder group(SlotGroupType type, Supplier<SlotGroup<Fluid, FluidStack, FluidResourceSlot>> group) {
            if (this.types.contains(type)) throw new IllegalArgumentException("duplicate slot group");
            this.types.add(type);
            this.groups.add(group);
            return this;
        }

        public Builder single(SlotGroupType type, Supplier<FluidResourceSlot> slot) {
            return group(type, () -> SlotGroup.ofFluid(slot.get()));
        }

        public MachineFluidStorage build() {
            if (this.groups.isEmpty()) return empty();
            @SuppressWarnings("unchecked")
            SlotGroup<Fluid, FluidStack, FluidResourceSlot>[] built = new SlotGroup[this.groups.size()];
            for (int i = 0; i < built.length; i++) built[i] = this.groups.get(i).get();
            return new MachineFluidStorageImpl(this.types.toArray(new SlotGroupType[0]), built);
        }

        @Override public MachineFluidStorage get() { return build(); }
    }
}
