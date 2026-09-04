/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.impl.storage.MachineItemStorageImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** MachineLib 0.2-compatible grouped item storage backed by Forge item capabilities. */
public interface MachineItemStorage extends Container, Iterable<ItemResourceSlot> {
    static MachineItemStorage create(ItemResourceSlot... slots) {
        return slots.length == 0 ? empty() : new MachineItemStorageImpl(slots);
    }

    static Supplier<MachineItemStorage> of(ItemResourceSlot.Builder... slots) {
        return () -> {
            ItemResourceSlot[] built = new ItemResourceSlot[slots.length];
            for (int i = 0; i < slots.length; i++) built[i] = slots[i].build();
            return create(built);
        };
    }

    static Builder builder() { return new Builder(); }
    static MachineItemStorage empty() { return MachineItemStorageImpl.EMPTY; }

    int size();
    ItemResourceSlot slot(int slot);
    default ItemResourceSlot getSlot(int slot) { return slot(slot); }
    ItemResourceSlot[] getSlots();
    ResourceFilter<Item> getStrictFilter(int slot);
    SlotGroup<Item, ItemStack, ItemResourceSlot> getGroup(SlotGroupType type);
    void setListener(Runnable listener);
    IItemHandler getExposedStorage(ResourceFlow flow);

    boolean consumeOne(Item resource);
    boolean consumeOne(Item resource, CompoundTag tag);
    long consume(Item resource, long amount);
    long consume(Item resource, CompoundTag tag, long amount);
    Item consumeOne(int slot);
    boolean consumeOne(int slot, Item resource);
    boolean consumeOne(int slot, Item resource, CompoundTag tag);
    long consume(int slot, long amount);
    long consume(int slot, Item resource, long amount);
    long consume(int slot, Item resource, CompoundTag tag, long amount);
    boolean consumeOne(int start, int len, Item resource);
    boolean consumeOne(int start, int len, Item resource, CompoundTag tag);
    long consume(int start, int len, Item resource, long amount);
    long consume(int start, int len, Item resource, CompoundTag tag, long amount);

    default Container getCraftingView(SlotGroupType type) {
        SlotGroup<Item, ItemStack, ItemResourceSlot> group = getGroup(type);
        return new net.minecraft.world.SimpleContainer(java.util.stream.IntStream.range(0, group.size())
                .mapToObj(i -> group.getSlot(i).toStack()).toArray(ItemStack[]::new));
    }

    final class Builder implements Supplier<MachineItemStorage> {
        private final List<SlotGroupType> types = new ArrayList<>();
        private final List<Supplier<SlotGroup<Item, ItemStack, ItemResourceSlot>>> groups = new ArrayList<>();
        private final List<ItemResourceSlot.Builder> legacySlots = new ArrayList<>();

        private Builder() {}

        public Builder group(SlotGroupType type, Supplier<SlotGroup<Item, ItemStack, ItemResourceSlot>> group) {
            if (this.types.contains(type)) throw new IllegalArgumentException("duplicate slot group");
            this.types.add(type);
            this.groups.add(group);
            return this;
        }

        public Builder single(SlotGroupType type, Supplier<ItemResourceSlot> slot) {
            return group(type, () -> SlotGroup.ofItem(slot.get()));
        }

        /** Compatibility with the initial Forge-backport API. */
        public Builder add(ItemResourceSlot.Builder slot) { this.legacySlots.add(slot); return this; }
        public Builder add3x3Grid(InputType type, int xOffset, int yOffset) { return addGrid(type, xOffset, yOffset, 3, 3); }
        public Builder addGrid(InputType type, int xOffset, int yOffset, int width, int height) {
            if (width <= 0 || height <= 0) throw new IllegalArgumentException();
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
                this.legacySlots.add(ItemResourceSlot.builder(type).pos(x * 18 + xOffset, y * 18 + yOffset));
            return this;
        }

        public MachineItemStorage build() {
            if (!this.groups.isEmpty()) {
                @SuppressWarnings("unchecked")
                SlotGroup<Item, ItemStack, ItemResourceSlot>[] built = new SlotGroup[this.groups.size()];
                for (int i = 0; i < built.length; i++) built[i] = this.groups.get(i).get();
                return new MachineItemStorageImpl(this.types.toArray(new SlotGroupType[0]), built);
            }
            ItemResourceSlot[] built = new ItemResourceSlot[this.legacySlots.size()];
            for (int i = 0; i < built.length; i++) built[i] = this.legacySlots.get(i).build();
            return create(built);
        }

        @Override public MachineItemStorage get() { return build(); }
    }
}
