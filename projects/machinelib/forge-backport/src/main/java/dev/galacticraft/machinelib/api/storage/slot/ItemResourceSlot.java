/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage.slot;

import com.mojang.datafixers.util.Pair;
import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Loader-neutral item slot with the MachineLib 0.2 builder/filter surface used
 * by Galacticraft 1.20.1. A slot group assigns the historical InputType after
 * construction; Forge exposure reads that mapped internal policy.
 */
public final class ItemResourceSlot {
    private dev.galacticraft.machinelib.api.transfer.InputType inputType;
    private final ItemSlotDisplay display;
    private final ResourceFilter<Item> filter;
    private final ResourceFilter<Item> strictFilter;
    private final int capacity;

    private Item resource;
    private CompoundTag tag;
    private int amount;
    private long modifications;
    private Runnable listener;

    private ItemResourceSlot(dev.galacticraft.machinelib.api.transfer.InputType inputType,
                             ItemSlotDisplay display,
                             ResourceFilter<Item> filter,
                             ResourceFilter<Item> strictFilter,
                             int capacity) {
        if (capacity <= 0 || capacity > 64) throw new IllegalArgumentException("capacity must be 1..64");
        this.inputType = inputType;
        this.display = display;
        this.filter = filter;
        this.strictFilter = strictFilter;
        this.capacity = capacity;
    }

    /** Historical MachineLib 0.2 builder used by Galacticraft 1.20.1. */
    public static Builder builder() {
        return new Builder(dev.galacticraft.machinelib.api.transfer.InputType.TRANSFER);
    }

    /** Compatibility builder retained for the early Forge-backport storage tests. */
    public static Builder builder(dev.galacticraft.machinelib.api.transfer.InputType inputType) {
        return new Builder(inputType);
    }

    public static ItemResourceSlot create(ItemSlotDisplay display, ResourceFilter<Item> filter) {
        return create(display, filter, filter, 64);
    }

    public static ItemResourceSlot create(ItemSlotDisplay display, ResourceFilter<Item> filter, ResourceFilter<Item> strictFilter) {
        return create(display, filter, strictFilter, 64);
    }

    public static ItemResourceSlot create(ItemSlotDisplay display, ResourceFilter<Item> filter,
                                          ResourceFilter<Item> strictFilter, int capacity) {
        return new ItemResourceSlot(dev.galacticraft.machinelib.api.transfer.InputType.TRANSFER,
                display, filter, strictFilter, capacity);
    }

    public static ItemResourceSlot create(dev.galacticraft.machinelib.api.transfer.InputType inputType,
                                          ItemSlotDisplay display, ResourceFilter<Item> filter, int capacity) {
        return new ItemResourceSlot(inputType, display, filter, filter, capacity);
    }

    public dev.galacticraft.machinelib.api.transfer.InputType inputType() { return this.inputType; }

    /** Called by MachineItemStorage when the slot is attached to a 0.2 SlotGroupType. */
    public void assignInputType(dev.galacticraft.machinelib.impl.storage.slot.InputType inputType) {
        this.inputType = inputType.toInternal();
    }

    public Item getResource() { return this.resource; }
    public long getAmount() { return this.amount; }
    public CompoundTag getTag() { return this.tag; }
    public CompoundTag copyTag() { return this.tag == null ? null : this.tag.copy(); }
    public long getCapacity() { return this.capacity; }
    public long getRealCapacity() { return this.capacity; }
    public long getCapacityFor(Item item) { return Math.min(this.capacity, item.getMaxStackSize()); }
    public ResourceFilter<Item> getFilter() { return this.filter; }
    public ResourceFilter<Item> getStrictFilter() { return this.strictFilter; }
    public ItemSlotDisplay getDisplay() { return this.display; }
    public long getModifications() { return this.modifications; }
    public boolean isEmpty() { return this.resource == null || this.amount <= 0; }
    public boolean isFull() { return !this.isEmpty() && this.amount >= this.getCapacityFor(this.resource); }
    public void setListener(Runnable listener) { this.listener = listener; }

    public boolean contains(Item item) { return this.contains(item, null, false); }
    public boolean contains(Item item, CompoundTag tag) { return this.contains(item, tag, true); }
    private boolean contains(Item item, CompoundTag tag, boolean matchTag) {
        return !this.isEmpty() && this.resource == item && (!matchTag || ResourceFilters.tagsEqual(this.tag, tag));
    }

    public boolean canInsert(Item item) { return this.canInsert(item, null, 1, false); }
    public boolean canInsert(Item item, CompoundTag tag) { return this.canInsert(item, tag, 1, true); }
    public boolean canInsert(Item item, long amount) { return this.canInsert(item, null, amount, false); }
    public boolean canInsert(Item item, CompoundTag tag, long amount) { return this.canInsert(item, tag, amount, true); }
    private boolean canInsert(Item item, CompoundTag tag, long requested, boolean matchTag) {
        if (item == null || requested < 0 || !this.filter.test(item, tag)) return false;
        if (requested == 0) return true;
        if (!this.isEmpty() && (this.resource != item || (matchTag && !ResourceFilters.tagsEqual(this.tag, tag)))) return false;
        return requested <= this.getCapacityFor(item) - this.amount;
    }

    public long tryInsert(Item item, long amount) { return this.tryInsert(item, null, amount, false); }
    public long tryInsert(Item item, CompoundTag tag, long amount) { return this.tryInsert(item, tag, amount, true); }
    private long tryInsert(Item item, CompoundTag tag, long requested, boolean matchTag) {
        if (item == null || requested <= 0 || !this.filter.test(item, tag)) return 0;
        if (!this.isEmpty() && (this.resource != item || (matchTag && !ResourceFilters.tagsEqual(this.tag, tag)))) return 0;
        return Math.min(requested, this.getCapacityFor(item) - this.amount);
    }

    public long insert(Item item, long amount) { return this.insert(item, null, amount); }
    public long insert(Item item, CompoundTag tag, long amount) {
        long inserted = this.tryInsert(item, tag, amount);
        if (inserted <= 0) return 0;
        if (this.isEmpty()) {
            this.resource = item;
            this.tag = cleanTag(tag);
        }
        this.amount += (int) inserted;
        this.markModified();
        return inserted;
    }

    public boolean canExtract(long amount) { return amount >= 0 && amount <= this.amount; }
    public boolean canExtract(Item item, long amount) { return this.contains(item) && this.canExtract(amount); }
    public boolean canExtract(Item item, CompoundTag tag, long amount) { return this.contains(item, tag) && this.canExtract(amount); }
    public long tryExtract(long amount) { return amount <= 0 ? 0 : Math.min(amount, this.amount); }
    public long tryExtract(Item item, long amount) { return this.contains(item) ? this.tryExtract(amount) : 0; }
    public long tryExtract(Item item, CompoundTag tag, long amount) { return this.contains(item, tag) ? this.tryExtract(amount) : 0; }

    public Item extractOne() {
        if (this.isEmpty()) return null;
        Item result = this.resource;
        this.extract(1);
        return result;
    }

    public long extract(long amount) {
        long extracted = this.tryExtract(amount);
        if (extracted <= 0) return 0;
        this.amount -= (int) extracted;
        if (this.amount == 0) {
            this.resource = null;
            this.tag = null;
        }
        this.markModified();
        return extracted;
    }

    public Item consumeOne() { return this.extractOne(); }
    public boolean consumeOne(Item item) { return this.consumeOne(item, null); }
    public boolean consumeOne(Item item, CompoundTag tag) { return this.consume(item, tag, 1) == 1; }
    public long consume(long amount) { return this.extract(amount); }
    public long consume(Item item, long amount) { return this.contains(item) ? this.extract(amount) : 0; }
    public long consume(Item item, CompoundTag tag, long amount) { return this.contains(item, tag) ? this.extract(amount) : 0; }

    public void set(Item item, long amount) { this.set(item, null, amount); }
    public void set(Item item, CompoundTag tag, long amount) {
        if (item == null) {
            if (amount != 0) throw new IllegalArgumentException("null item with nonzero amount");
            this.resource = null;
            this.tag = null;
            this.amount = 0;
            this.markModified();
            return;
        }
        if (amount < 0 || amount > this.getCapacityFor(item)) throw new IllegalArgumentException("amount outside slot capacity");
        if (amount > 0 && !this.filter.test(item, tag)) throw new IllegalArgumentException("item rejected by slot filter");
        this.resource = amount == 0 ? null : item;
        this.tag = amount == 0 ? null : cleanTag(tag);
        this.amount = (int) amount;
        this.markModified();
    }

    public ItemStack toStack() {
        if (this.isEmpty()) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(this.resource, this.amount);
        if (this.tag != null) stack.setTag(this.tag.copy());
        return stack;
    }

    public void setStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            this.set(null, null, 0);
            return;
        }
        this.set(stack.getItem(), stack.getTag(), Math.min(stack.getCount(), this.getCapacityFor(stack.getItem())));
    }

    private void markModified() {
        this.modifications++;
        if (this.listener != null) this.listener.run();
    }

    private static CompoundTag cleanTag(CompoundTag tag) {
        return tag == null || tag.isEmpty() ? null : tag.copy();
    }

    public static final class Builder {
        private final dev.galacticraft.machinelib.api.transfer.InputType initialInputType;
        private int x;
        private int y;
        private Pair<ResourceLocation, ResourceLocation> icon;
        private ResourceFilter<Item> filter = ResourceFilters.any();
        private ResourceFilter<Item> strictFilter;
        private int capacity = 64;

        private Builder(dev.galacticraft.machinelib.api.transfer.InputType initialInputType) {
            this.initialInputType = initialInputType;
        }

        public Builder pos(int x, int y) { this.x = x; this.y = y; return this; }
        public Builder x(int x) { this.x = x; return this; }
        public Builder y(int y) { this.y = y; return this; }
        public Builder icon(Pair<ResourceLocation, ResourceLocation> icon) { this.icon = icon; return this; }
        public Builder filter(ResourceFilter<Item> filter) { this.filter = filter; return this; }
        public Builder strictFilter(ResourceFilter<Item> strictFilter) { this.strictFilter = strictFilter; return this; }
        public Builder capacity(int capacity) { this.capacity = capacity; return this; }

        public ItemResourceSlot build() {
            ResourceFilter<Item> strict = this.strictFilter == null ? this.filter : this.strictFilter;
            return new ItemResourceSlot(this.initialInputType,
                    ItemSlotDisplay.create(this.x, this.y, this.icon), this.filter, strict, this.capacity);
        }
    }
}
