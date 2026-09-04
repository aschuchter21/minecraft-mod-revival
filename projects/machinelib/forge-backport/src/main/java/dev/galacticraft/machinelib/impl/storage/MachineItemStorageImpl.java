/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.forge.item.ForgeItemStorageAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

public final class MachineItemStorageImpl implements MachineItemStorage {
    public static final MachineItemStorageImpl EMPTY = new MachineItemStorageImpl(new ItemResourceSlot[0]);

    private final ItemResourceSlot[] slots;
    private final Map<SlotGroupType, SlotGroup<Item, ItemStack, ItemResourceSlot>> groups = new IdentityHashMap<>();
    private Runnable listener;

    public MachineItemStorageImpl(ItemResourceSlot[] slots) {
        this.slots = slots.clone();
        for (ItemResourceSlot slot : this.slots) slot.setListener(this::markModified);
    }

    public MachineItemStorageImpl(SlotGroupType[] types, SlotGroup<Item, ItemStack, ItemResourceSlot>[] groups) {
        if (types.length != groups.length) throw new IllegalArgumentException("type/group length mismatch");
        int total = 0;
        for (SlotGroup<Item, ItemStack, ItemResourceSlot> group : groups) total += group.size();
        this.slots = new ItemResourceSlot[total];
        int offset = 0;
        for (int i = 0; i < groups.length; i++) {
            SlotGroup<Item, ItemStack, ItemResourceSlot> group = groups[i];
            this.groups.put(types[i], group);
            for (ItemResourceSlot slot : group) {
                slot.assignInputType(types[i].inputType());
                slot.setListener(this::markModified);
                this.slots[offset++] = slot;
            }
        }
    }

    @Override public int size() { return this.slots.length; }
    @Override public int getContainerSize() { return this.slots.length; }
    @Override public ItemResourceSlot slot(int slot) { return this.slots[slot]; }
    @Override public ItemResourceSlot[] getSlots() { return this.slots.clone(); }
    @Override public ResourceFilter<Item> getStrictFilter(int slot) { return this.slots[slot].getStrictFilter(); }
    @Override public SlotGroup<Item, ItemStack, ItemResourceSlot> getGroup(SlotGroupType type) {
        SlotGroup<Item, ItemStack, ItemResourceSlot> group = this.groups.get(type);
        if (group == null) throw new IllegalArgumentException("Unknown slot group: " + type.name().getString());
        return group;
    }
    @Override public void setListener(Runnable listener) { this.listener = listener; }
    @Override public IItemHandler getExposedStorage(ResourceFlow flow) { return new ForgeItemStorageAdapter(this, flow); }
    @Override public Iterator<ItemResourceSlot> iterator() { return Arrays.asList(this.slots).iterator(); }

    @Override public boolean isEmpty() {
        for (ItemResourceSlot slot : this.slots) if (!slot.isEmpty()) return false;
        return true;
    }

    @Override public boolean consumeOne(Item resource) { return consume(resource, 1) == 1; }
    @Override public boolean consumeOne(Item resource, CompoundTag tag) { return consume(resource, tag, 1) == 1; }
    @Override public long consume(Item resource, long amount) { return consume(resource, null, amount); }
    @Override public long consume(Item resource, CompoundTag tag, long amount) {
        long remaining = Math.max(0, amount);
        long removed = 0;
        for (ItemResourceSlot slot : this.slots) {
            if (remaining == 0) break;
            long current = tag == null ? slot.tryExtract(resource, remaining) : slot.tryExtract(resource, tag, remaining);
            if (current > 0) {
                removed += slot.extract(current);
                remaining -= current;
            }
        }
        return removed;
    }

    @Override public Item consumeOne(int slot) { return this.slots[slot].consumeOne(); }
    @Override public boolean consumeOne(int slot, Item resource) { return this.slots[slot].consumeOne(resource); }
    @Override public boolean consumeOne(int slot, Item resource, CompoundTag tag) { return this.slots[slot].consumeOne(resource, tag); }
    @Override public long consume(int slot, long amount) { return this.slots[slot].consume(amount); }
    @Override public long consume(int slot, Item resource, long amount) { return this.slots[slot].consume(resource, amount); }
    @Override public long consume(int slot, Item resource, CompoundTag tag, long amount) { return this.slots[slot].consume(resource, tag, amount); }
    @Override public boolean consumeOne(int start, int len, Item resource) { return consume(start, len, resource, 1) == 1; }
    @Override public boolean consumeOne(int start, int len, Item resource, CompoundTag tag) { return consume(start, len, resource, tag, 1) == 1; }
    @Override public long consume(int start, int len, Item resource, long amount) { return consume(start, len, resource, null, amount); }
    @Override public long consume(int start, int len, Item resource, CompoundTag tag, long amount) {
        if (start < 0 || len < 0 || start + len > this.slots.length) throw new IndexOutOfBoundsException();
        long remaining = Math.max(0, amount);
        long removed = 0;
        for (int i = start; i < start + len && remaining > 0; i++) {
            ItemResourceSlot slot = this.slots[i];
            long current = tag == null ? slot.tryExtract(resource, remaining) : slot.tryExtract(resource, tag, remaining);
            if (current > 0) {
                removed += slot.extract(current);
                remaining -= current;
            }
        }
        return removed;
    }

    @Override public ItemStack getItem(int slot) { return this.slots[slot].toStack(); }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (amount <= 0) return ItemStack.EMPTY;
        ItemResourceSlot resourceSlot = this.slots[slot];
        if (resourceSlot.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = resourceSlot.toStack();
        int removed = (int) resourceSlot.extract(Math.min(amount, result.getCount()));
        result.setCount(removed);
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemResourceSlot resourceSlot = this.slots[slot];
        ItemStack result = resourceSlot.toStack();
        if (!result.isEmpty()) resourceSlot.set(null, null, 0);
        return result;
    }
    @Override public void setItem(int slot, ItemStack stack) { this.slots[slot].setStack(stack); }
    @Override public void setChanged() { this.markModified(); }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() { for (ItemResourceSlot slot : this.slots) if (!slot.isEmpty()) slot.set(null, null, 0); }

    private void markModified() { if (this.listener != null) this.listener.run(); }
}
