/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

/**
 * Loader-neutral MachineLib 0.3 fluid slot. Amounts remain in Fabric-era internal
 * units (81,000 units per bucket) so Galacticraft's 1.20.1 machine math stays unchanged.
 */
public final class FluidResourceSlot {
    public static final long INTERNAL_UNITS_PER_BUCKET = 81_000L;
    public static final long INTERNAL_UNITS_PER_MB = 81L;

    private final InputType inputType;
    private final TankDisplay display;
    private final long capacity;
    private final ResourceFilter<Fluid> filter;

    private Fluid resource;
    private CompoundTag tag;
    private long amount;
    private long modifications;
    private Runnable listener;

    private FluidResourceSlot(InputType inputType, TankDisplay display, long capacity, ResourceFilter<Fluid> filter) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.inputType = inputType;
        this.display = display;
        this.capacity = capacity;
        this.filter = filter;
    }

    public static Builder builder(InputType inputType) { return new Builder(inputType); }
    public static FluidResourceSlot create(InputType inputType, TankDisplay display, long capacity, ResourceFilter<Fluid> filter) {
        return new FluidResourceSlot(inputType, display, capacity, filter);
    }

    public InputType inputType() { return this.inputType; }
    public Fluid getResource() { return this.resource; }
    public long getAmount() { return this.amount; }
    public CompoundTag getTag() { return this.tag; }
    public CompoundTag copyTag() { return this.tag == null ? null : this.tag.copy(); }
    public long getCapacity() { return this.capacity; }
    public long getRealCapacity() { return this.capacity; }
    public long getCapacityFor(Fluid fluid) { return this.capacity; }
    public ResourceFilter<Fluid> getFilter() { return this.filter; }
    public TankDisplay getDisplay() { return this.display; }
    public boolean isHidden() { return this.display == null; }
    public long getModifications() { return this.modifications; }
    public boolean isEmpty() { return this.resource == null || this.amount <= 0; }
    public boolean isFull() { return this.amount >= this.capacity; }
    public void setListener(Runnable listener) { this.listener = listener; }

    public boolean contains(Fluid fluid) { return !this.isEmpty() && this.resource == fluid; }
    public boolean contains(Fluid fluid, CompoundTag tag) {
        return this.contains(fluid) && ResourceFilters.tagsEqual(this.tag, tag);
    }

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
        private final InputType inputType;
        private boolean hidden;
        private int x;
        private int y;
        private int width = 16;
        private int height = 48;
        private ResourceFilter<Fluid> filter = ResourceFilters.any();
        private long capacity = INTERNAL_UNITS_PER_BUCKET;

        private Builder(InputType inputType) { this.inputType = inputType; }
        public Builder pos(int x, int y) { this.x = x; this.y = y; return this; }
        public Builder hidden() { this.hidden = true; return this; }
        public Builder x(int x) { if (this.hidden) throw new UnsupportedOperationException("hidden"); this.x = x; return this; }
        public Builder y(int y) { if (this.hidden) throw new UnsupportedOperationException("hidden"); this.y = y; return this; }
        public Builder width(int width) { if (this.hidden) throw new UnsupportedOperationException("hidden"); this.width = width; return this; }
        public Builder height(int height) { if (this.hidden) throw new UnsupportedOperationException("hidden"); this.height = height; return this; }
        public Builder filter(ResourceFilter<Fluid> filter) { this.filter = filter; return this; }
        public Builder capacity(long capacity) { this.capacity = capacity; return this; }
        public FluidResourceSlot build() {
            if (this.capacity <= 0 || this.width < 0 || this.height < 0) throw new IllegalArgumentException();
            return FluidResourceSlot.create(this.inputType,
                    this.hidden ? null : TankDisplay.create(this.x, this.y, this.width, this.height),
                    this.capacity, this.filter);
        }
    }
}
