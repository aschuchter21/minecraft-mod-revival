/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.forge.item;

import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/** Forge item capability view over MachineLib's loader-neutral slots. */
public final class ForgeItemStorageAdapter implements IItemHandlerModifiable {
    private final MachineItemStorage storage;
    private final ResourceFlow flow;

    public ForgeItemStorageAdapter(MachineItemStorage storage, ResourceFlow flow) {
        this.storage = storage;
        this.flow = flow;
    }

    @Override public int getSlots() { return this.storage.size(); }
    @Override public ItemStack getStackInSlot(int slot) { return this.storage.slot(slot).toStack(); }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty() || !allowsInsert()) return stack;
        ItemResourceSlot target = this.storage.slot(slot);
        if (!target.inputType().externalInsertion()) return stack;

        CompoundTag tag = stack.getTag();
        long accepted = target.tryInsert(stack.getItem(), tag, stack.getCount());
        if (accepted <= 0) return stack;
        if (!simulate) target.insert(stack.getItem(), tag, accepted);

        int remainder = stack.getCount() - (int) accepted;
        if (remainder <= 0) return ItemStack.EMPTY;
        ItemStack copy = stack.copy();
        copy.setCount(remainder);
        return copy;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || !allowsExtract()) return ItemStack.EMPTY;
        ItemResourceSlot source = this.storage.slot(slot);
        if (!source.inputType().externalExtraction() || source.isEmpty()) return ItemStack.EMPTY;

        int extracted = (int) Math.min(Integer.MAX_VALUE, source.tryExtract(amount));
        if (extracted <= 0) return ItemStack.EMPTY;
        ItemStack result = source.toStack();
        result.setCount(extracted);
        if (!simulate) source.extract(extracted);
        return result;
    }

    @Override public int getSlotLimit(int slot) { return (int) Math.min(Integer.MAX_VALUE, this.storage.slot(slot).getCapacity()); }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack == null || stack.isEmpty() || !allowsInsert()) return false;
        ItemResourceSlot target = this.storage.slot(slot);
        return target.inputType().externalInsertion() && target.tryInsert(stack.getItem(), stack.getTag(), 1) > 0;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.storage.slot(slot).setStack(stack == null ? ItemStack.EMPTY : stack);
    }

    private boolean allowsInsert() { return this.flow == ResourceFlow.INPUT || this.flow == ResourceFlow.BOTH; }
    private boolean allowsExtract() { return this.flow == ResourceFlow.OUTPUT || this.flow == ResourceFlow.BOTH; }
}
