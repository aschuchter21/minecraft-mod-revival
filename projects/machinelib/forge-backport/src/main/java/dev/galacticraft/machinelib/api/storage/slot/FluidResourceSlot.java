/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

/**
 * Loader-neutral fluid slot with the MachineLib 0.2 builder surface used by
 * Galacticraft 1.20.1. Amounts remain in the historical Fabric transfer units
 * (81,000 units per bucket); Forge adapters convert only at their boundary.
 */
public final class FluidResourceSlot {
    public static final long INTERNAL_UNITS_PER_BUCKET = 81_000L;
    public static final long INTERNAL_UNITS_PER_MB = 81L;

    private dev.galacticraft.machinelib.api.transfer.InputType inputType;
    private final TankDisplay display;
    private final long capacity;
    private final ResourceFilter<Fluid> filter;
    private final ResourceFilter<Fluid> strictFilter;

    private Fluid resource;
    private CompoundTag tag;
    private long amount;
    private long modifications;
    private Runnable listener;

    private FluidResourceSlot(dev.galacticraft.machinelib.api.transfer.InputType inputType,
                              TankDisplay display, long capacity,
                              ResourceFilter<Fluid> filter, ResourceFilter<Fluid> strictFilter) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.inputType = inputType;
        this.display = display;
        this.capacity = capacity;
        this.filter = filter;
        this.strictFilter = strictFilter;
    }

    public static Builder builder() {
        return new Builder(dev.galacticraft.machinelib.api.transfer.InputType.TRANSFER);
    }

    public static Builder builder(dev.galacticraft.machinelib.api.transfer.InputType inputType) {
        return new Builder(inputType);
    }

    public static FluidResourceSlot create(TankDisplay display, long capacity, ResourceFilter<Fluid> filter) {
        return new FluidResourceSlot(dev.galacticraft.machinelib.api.transfer.InputType.TRANSFER,
                display, capacity, filter, filter);
    }

    public static FluidResourceSlot create(dev.galacticraft.machinelib.api.transfer.InputType inputType,
                                           TankDisplay display, long capacity, ResourceFilter<Fluid> filter) {
        return new FluidResourceSlot(inputType, display, capacity, filter, filter);
    }

    public dev.galacticraft.machinelib.api.transfer.InputType inputType() { return this.inputType; }

    public void assignInputType(dev.galacticraft.machinelib.impl.storage.slot.InputType inputType) {
        this.inputType = inputType.toInternal();
    }

    public Fluid getResource() { return this.resource; }
    public long getAmount() { return this.amount; }
    public CompoundTag getTag() { return this.tag; }
    public CompoundTag copyTag() { return this.tag == null ? null : this.tag.copy(); }
    public long getCapacity() { return this.capacity; }
    public long getRealCapacity() { return this.capacity; }
    public long getCapacityFor(Fluid fluid) { return this.capacity; }
    public ResourceFilter<Fluid> getFilter() { return this.filter; }
    public ResourceFilter<Fluid> getStrictFilter() { return this.strictFilter; }
    public TankDisplay getDisplay() { return this.display; }
    public boolean isHidden() { return this.display == null; }
    public long getModifications() { return this.modifications; }
    public boolean isEmpty() { return this.resource == null || this.amount <= 0; }
    public boolean isFull() { return this.amount >= this.capacity; }
    public void setListener(Runnable listener) { this.listener = listener; }

    public boolean contains(Fluid fluid) { return !this.isEmpty() && this.resource == fluid; }
    public boolean contains(Fluid fluid, CompoundTag tag) { return this.contains(fluid) && ResourceFilters.tagsEqual(this.tag, tag); }

    public boolean canInsert(Fluid fluid) { return this.canInsert(fluid, null, 1, false); }
    public boolean canInsert(Fluid fluid, CompoundTag tag) { return this.canInsert(fluid, tag, 1, true); }
    public boolean canInsert(Fluid fluid, long amount) { return this.canInsert(fluid, null, amount, false); }
    public boolean canInsert(Fluid fluid, CompoundTag tag, long amount) { return this.canInsert(fluid, tag, amount, true); }
    private boolean canInsert(Fluid fluid, CompoundTag tag, long requested, boolean matchTag) {
        if (fluid == null || requested < 0 || !this.filter.test(fluid, tag)) return false;
        if (!this.isEmpty() && (this.resource != fluid || (matchTag && !ResourceFilters.tagsEqual(this.tag, tag)))) return false;
        return requested <= this.capacity - this.amount;
    }

    public long tryInsert(Fluid fluid, long amount) { return this.tryInsert(fluid, null, amount, false); }
    public long tryInsert(Fluid fluid, CompoundTag tag, long amount) { return this.tryInsert(fluid, tag, amount, true); }
    private long tryInsert(Fluid fluid, CompoundTag tag, long requested, boolean matchTag) {
        if (fluid == null || requested <= 0 || !this.filter.test(fluid, tag)) return 0;
        if (!this.isEmpty() && (this.resource != fluid || (matchTag && !ResourceFilters.tagsEqual(this.tag, tag)))) return 0;
        return Math.min(requested, this.capacity - this.amount);
    }

    public long insert(Fluid fluid, long amount) { return this.insert(fluid, null, amount); }
    public long insert(Fluid fluid, CompoundTag tag, long amount) {
        long inserted = this.tryInsert(fluid, tag, amount);
        if (inserted <= 0) return 0;
        if (this.isEmpty()) {
            this.resource = fluid;
            this.tag = cleanTag(tag);
        }
        this.amount += inserted;
        this.markModified();
        return inserted;
    }

    public boolean canExtract(long amount) { return amount >= 0 && amount <= this.amount; }
    public boolean canExtract(Fluid fluid, long amount) { return this.contains(fluid) && this.canExtract(amount); }
    public boolean canExtract(Fluid fluid, CompoundTag tag, long amount) { return this.contains(fluid, tag) && this.canExtract(amount); }
    public long tryExtract(long amount) { return amount <= 0 ? 0 : Math.min(amount, this.amount); }
    public long tryExtract(Fluid fluid, long amount) { return this.contains(fluid) ? this.tryExtract(amount) : 0; }
    public long tryExtract(Fluid fluid, CompoundTag tag, long amount) { return this.contains(fluid, tag) ? this.tryExtract(amount) : 0; }

    public Fluid extractOne() {
        if (this.isEmpty()) return null;
        Fluid result = this.resource;
        this.extract(1);
        return result;
    }

    public long extract(long amount) {
        long extracted = this.tryExtract(amount);
        if (extracted <= 0) return 0;
        this.amount -= extracted;
        if (this.amount == 0) {
            this.resource = null;
            this.tag = null;
        }
        this.markModified();
        return extracted;
    }

    public void set(Fluid fluid, long amount) { this.set(fluid, null, amount); }
    public void set(Fluid fluid, CompoundTag tag, long amount) {
        if (fluid == null) {
            if (amount != 0) throw new IllegalArgumentException("null fluid with nonzero amount");
            this.resource = null;
            this.tag = null;
            this.amount = 0;
            this.markModified();
            return;
        }
        if (amount < 0 || amount > this.capacity) throw new IllegalArgumentException("amount outside tank capacity");
        if (amount > 0 && !this.filter.test(fluid, tag)) throw new IllegalArgumentException("fluid rejected by tank filter");
        this.resource = amount == 0 ? null : fluid;
        this.tag = amount == 0 ? null : cleanTag(tag);
        this.amount = amount;
        this.markModified();
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
        private int width = 16;
        private int height = 48;
        private ResourceFilter<Fluid> filter = ResourceFilters.any();
        private ResourceFilter<Fluid> strictFilter;
        private long capacity = INTERNAL_UNITS_PER_BUCKET;

        private Builder(dev.galacticraft.machinelib.api.transfer.InputType initialInputType) {
            this.initialInputType = initialInputType;
        }

        public Builder pos(int x, int y) { this.x = x; this.y = y; return this; }
        public Builder x(int x) { this.x = x; return this; }
        public Builder y(int y) { this.y = y; return this; }
        public Builder width(int width) { this.width = width; return this; }
        public Builder height(int height) { this.height = height; return this; }
        public Builder hidden() { this.height = 0; return this; }
        public Builder filter(ResourceFilter<Fluid> filter) { this.filter = filter; return this; }
        public Builder strictFilter(ResourceFilter<Fluid> strictFilter) { this.strictFilter = strictFilter; return this; }
        public Builder capacity(long capacity) { this.capacity = capacity; return this; }

        public FluidResourceSlot build() {
            if (this.capacity <= 0 || this.width < 0 || this.height < 0) throw new IllegalArgumentException();
            ResourceFilter<Fluid> strict = this.strictFilter == null ? this.filter : this.strictFilter;
            TankDisplay display = this.height == 0 ? null : TankDisplay.create(this.x, this.y, this.width, this.height);
            return new FluidResourceSlot(this.initialInputType, display, this.capacity, this.filter, strict);
        }
    }
}
