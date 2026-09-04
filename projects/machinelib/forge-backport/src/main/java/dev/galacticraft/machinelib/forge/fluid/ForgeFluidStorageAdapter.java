/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.forge.fluid;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Forge fluid capability view over MachineLib's 81,000-unit-per-bucket storage.
 * Only whole millibuckets cross the Forge capability boundary (81 internal units = 1 mB).
 */
public final class ForgeFluidStorageAdapter implements IFluidHandler {
    private final MachineFluidStorage storage;
    private final ResourceFlow flow;

    public ForgeFluidStorageAdapter(MachineFluidStorage storage, ResourceFlow flow) {
        this.storage = storage;
        this.flow = flow;
    }

    @Override public int getTanks() { return this.storage.size(); }

    @Override
    public FluidStack getFluidInTank(int tank) {
        FluidResourceSlot slot = this.storage.slot(tank);
        if (slot.isEmpty()) return FluidStack.EMPTY;
        int amountMb = saturatingInt(slot.getAmount() / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        if (amountMb <= 0) return FluidStack.EMPTY;
        return stack(slot.getResource(), slot.getTag(), amountMb);
    }

    @Override
    public int getTankCapacity(int tank) {
        return saturatingInt(this.storage.slot(tank).getCapacity() / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (stack == null || stack.isEmpty() || !allowsInsert()) return false;
        FluidResourceSlot slot = this.storage.slot(tank);
        return slot.inputType().externalInsertion() && slot.tryInsert(stack.getFluid(), stack.getTag(), FluidResourceSlot.INTERNAL_UNITS_PER_MB) >= FluidResourceSlot.INTERNAL_UNITS_PER_MB;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty() || resource.getAmount() <= 0 || !allowsInsert()) return 0;
        int remainingMb = resource.getAmount();
        int insertedMb = 0;

        for (FluidResourceSlot slot : this.storage) {
            if (remainingMb <= 0) break;
            if (!slot.inputType().externalInsertion()) continue;

            long requestedUnits = (long) remainingMb * FluidResourceSlot.INTERNAL_UNITS_PER_MB;
            long possibleUnits = slot.tryInsert(resource.getFluid(), resource.getTag(), requestedUnits);
            int acceptedMb = saturatingInt(possibleUnits / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
            if (acceptedMb <= 0) continue;

            if (action.execute()) {
                slot.insert(resource.getFluid(), resource.getTag(), (long) acceptedMb * FluidResourceSlot.INTERNAL_UNITS_PER_MB);
            }
            insertedMb += acceptedMb;
            remainingMb -= acceptedMb;
        }
        return insertedMb;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty() || resource.getAmount() <= 0 || !allowsExtract()) return FluidStack.EMPTY;
        return drainMatching(resource.getFluid(), resource.getTag(), resource.getAmount(), action, true);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || !allowsExtract()) return FluidStack.EMPTY;
        for (FluidResourceSlot slot : this.storage) {
            if (!slot.inputType().externalExtraction() || slot.isEmpty()) continue;
            return drainMatching(slot.getResource(), slot.getTag(), maxDrain, action, true);
        }
        return FluidStack.EMPTY;
    }

    private FluidStack drainMatching(Fluid fluid, CompoundTag tag, int maxDrain, FluidAction action, boolean matchTag) {
        int remainingMb = maxDrain;
        int extractedMb = 0;

        for (FluidResourceSlot slot : this.storage) {
            if (remainingMb <= 0) break;
            if (!slot.inputType().externalExtraction() || slot.isEmpty() || slot.getResource() != fluid) continue;
            if (matchTag && !ResourceFilters.tagsEqual(slot.getTag(), tag)) continue;

            long requestedUnits = (long) remainingMb * FluidResourceSlot.INTERNAL_UNITS_PER_MB;
            long possibleUnits = slot.tryExtract(fluid, tag, requestedUnits);
            int availableMb = saturatingInt(possibleUnits / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
            if (availableMb <= 0) continue;

            if (action.execute()) slot.extract((long) availableMb * FluidResourceSlot.INTERNAL_UNITS_PER_MB);
            extractedMb += availableMb;
            remainingMb -= availableMb;
        }

        return extractedMb <= 0 ? FluidStack.EMPTY : stack(fluid, tag, extractedMb);
    }

    private boolean allowsInsert() { return this.flow == ResourceFlow.INPUT || this.flow == ResourceFlow.BOTH; }
    private boolean allowsExtract() { return this.flow == ResourceFlow.OUTPUT || this.flow == ResourceFlow.BOTH; }

    private static FluidStack stack(Fluid fluid, CompoundTag tag, int amount) {
        FluidStack stack = new FluidStack(fluid, amount);
        if (tag != null && !tag.isEmpty()) stack.setTag(tag.copy());
        return stack;
    }

    private static int saturatingInt(long value) {
        if (value <= 0) return 0;
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }
}
