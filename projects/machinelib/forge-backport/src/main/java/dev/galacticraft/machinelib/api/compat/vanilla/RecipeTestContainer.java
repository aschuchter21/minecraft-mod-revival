/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.compat.vanilla;

import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** Read-only vanilla Container view used when asking RecipeManager for a match. */
public final class RecipeTestContainer implements Container {
    private final ItemResourceSlot[] slots;

    public static @NotNull RecipeTestContainer create(ItemResourceSlot... slots) {
        if (slots.length == 0) throw new IllegalArgumentException("recipe view requires at least one slot");
        return new RecipeTestContainer(slots.clone());
    }

    public static @NotNull RecipeTestContainer create(@NotNull MachineItemStorage storage, int start, int len) {
        if (start < 0 || len <= 0 || start + len > storage.size()) throw new IndexOutOfBoundsException();
        ItemResourceSlot[] slots = new ItemResourceSlot[len];
        for (int i = 0; i < len; i++) slots[i] = storage.getSlot(start + i);
        return new RecipeTestContainer(slots);
    }

    private RecipeTestContainer(ItemResourceSlot[] slots) {
        this.slots = slots;
    }

    @Override public int getContainerSize() { return this.slots.length; }
    @Override public boolean isEmpty() {
        for (ItemResourceSlot slot : this.slots) if (!slot.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getItem(int index) { return this.slots[index].toStack(); }
    @Override public ItemStack removeItem(int index, int count) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int index) { return ItemStack.EMPTY; }
    @Override public void setItem(int index, ItemStack stack) {}
    @Override public void setChanged() {}
    @Override public boolean stillValid(Player player) { return false; }
    @Override public boolean canPlaceItem(int index, ItemStack stack) { return false; }
    @Override public boolean canTakeItem(Container target, int index, ItemStack stack) { return false; }
    @Override public void clearContent() {}
}
