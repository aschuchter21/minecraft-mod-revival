/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage.slot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Ordered MachineLib 0.2-compatible slot group.  The original type exposed a
 * large Fabric transfer surface; Forge automation is handled by the flattened
 * storage adapters, while Galacticraft's machine code primarily relies on the
 * ordered group/getSlot API preserved here.
 */
public final class SlotGroup<Resource, Stack, Slot> implements Iterable<Slot> {
    private final Slot[] slots;

    private SlotGroup(Slot[] slots) {
        if (slots.length == 0) throw new IllegalArgumentException("no slots");
        this.slots = slots.clone();
    }

    public static Builder<Item, ItemStack, ItemResourceSlot> item() {
        return new Builder<>(slots -> new SlotGroup<>(slots), ItemResourceSlot[]::new);
    }

    public static Builder<Fluid, net.minecraftforge.fluids.FluidStack, FluidResourceSlot> fluid() {
        return new Builder<>(slots -> new SlotGroup<>(slots), FluidResourceSlot[]::new);
    }

    public static SlotGroup<Item, ItemStack, ItemResourceSlot> ofItem(ItemResourceSlot... slots) {
        return new SlotGroup<>(slots);
    }

    public static SlotGroup<Fluid, net.minecraftforge.fluids.FluidStack, FluidResourceSlot> ofFluid(FluidResourceSlot... slots) {
        return new SlotGroup<>(slots);
    }

    public int size() { return this.slots.length; }
    public Slot getSlot(int slot) { return this.slots[slot]; }
    public Slot[] getSlots() { return this.slots.clone(); }

    @Override
    public Iterator<Slot> iterator() {
        return Arrays.asList(this.slots).iterator();
    }

    public static final class Builder<Resource, Stack, Slot> {
        private final List<Supplier<Slot>> slots = new ArrayList<>();
        private final Function<Slot[], SlotGroup<Resource, Stack, Slot>> constructor;
        private final IntFunction<Slot[]> arrayProvider;

        private Builder(Function<Slot[], SlotGroup<Resource, Stack, Slot>> constructor, IntFunction<Slot[]> arrayProvider) {
            this.constructor = constructor;
            this.arrayProvider = arrayProvider;
        }

        public Builder<Resource, Stack, Slot> add(Supplier<Slot> slot) {
            this.slots.add(slot);
            return this;
        }

        public SlotGroup<Resource, Stack, Slot> build() {
            if (this.slots.isEmpty()) throw new IllegalArgumentException("no slots");
            Slot[] built = this.slots.stream().map(Supplier::get).toArray(this.arrayProvider);
            return this.constructor.apply(built);
        }
    }
}