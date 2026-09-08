/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.compat;

import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.mod.tag.GCTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Rocket fuel storage that preserves Galacticraft/MachineLib's historical
 * internal fluid unit contract while exposing a standard Forge mB boundary.
 *
 * <p>Galacticraft 1.20.1 uses 81,000 internal units per bucket. Forge fluid
 * handlers use 1,000 mB per bucket, so one Forge mB is exactly 81 internal
 * units. Keeping the internal amount as a long prevents the Rocket API,
 * countdown consumption and RocketData persistence from silently changing
 * units during the loader port.</p>
 */
public final class RocketFuelTank implements IFluidHandler {
    private static final String NBT_FLUID = "Fluid";
    private static final String NBT_AMOUNT = "Amount";

    private final long capacityInternal;
    private Fluid fluid = Fluids.EMPTY;
    private long amountInternal;
    private final Runnable onChanged;

    public RocketFuelTank(long capacityInternal, @Nullable Runnable onChanged) {
        if (capacityInternal < 0) throw new IllegalArgumentException("capacityInternal must be >= 0");
        this.capacityInternal = capacityInternal;
        this.onChanged = onChanged == null ? () -> {} : onChanged;
    }

    public long getAmountInternal() {
        return this.amountInternal;
    }

    public long getCapacityInternal() {
        return this.capacityInternal;
    }

    public @Nullable Fluid getFluid() {
        return this.amountInternal <= 0 || this.fluid == Fluids.EMPTY ? null : this.fluid;
    }

    public boolean isEmpty() {
        return this.getFluid() == null;
    }

    public long insertInternal(@NotNull Fluid fluid, long amount, boolean simulate) {
        if (amount <= 0 || !isValidFuel(fluid)) return 0;
        if (!this.isEmpty() && this.fluid != fluid) return 0;

        long inserted = Math.min(amount, this.capacityInternal - this.amountInternal);
        if (inserted <= 0) return 0;
        if (!simulate) {
            this.fluid = fluid;
            this.amountInternal += inserted;
            this.onChanged.run();
        }
        return inserted;
    }

    public long extractInternal(long amount, boolean simulate) {
        if (amount <= 0 || this.isEmpty()) return 0;
        long extracted = Math.min(amount, this.amountInternal);
        if (!simulate && extracted > 0) {
            this.amountInternal -= extracted;
            if (this.amountInternal == 0) this.fluid = Fluids.EMPTY;
            this.onChanged.run();
        }
        return extracted;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank != 0 || this.isEmpty()) return FluidStack.EMPTY;
        return new FluidStack(this.fluid, internalToMbFloor(this.amountInternal));
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank != 0) return 0;
        return internalToMbFloor(this.capacityInternal);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0 && !stack.isEmpty() && isValidFuel(stack.getFluid());
    }

    @Override
    public int fill(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !this.isFluidValid(0, resource)) return 0;
        long requestedInternal = mbToInternal(resource.getAmount());
        long insertedInternal = this.insertInternal(resource.getFluid(), requestedInternal, action.simulate());
        return internalToMbFloor(insertedInternal);
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || this.isEmpty() || resource.getFluid() != this.fluid) return FluidStack.EMPTY;
        return this.drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || this.isEmpty()) return FluidStack.EMPTY;
        int availableMb = internalToMbFloor(this.amountInternal);
        int drainedMb = Math.min(maxDrain, availableMb);
        if (drainedMb <= 0) return FluidStack.EMPTY;

        Fluid current = this.fluid;
        long extracted = this.extractInternal(mbToInternal(drainedMb), action.simulate());
        int extractedMb = internalToMbFloor(extracted);
        return extractedMb <= 0 ? FluidStack.EMPTY : new FluidStack(current, extractedMb);
    }

    public void save(@NotNull CompoundTag tag) {
        if (this.isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(this.fluid);
        if (id != null) tag.putString(NBT_FLUID, id.toString());
        tag.putLong(NBT_AMOUNT, this.amountInternal);
    }

    public void load(@NotNull CompoundTag tag) {
        this.fluid = Fluids.EMPTY;
        this.amountInternal = 0;
        if (!tag.contains(NBT_FLUID) || !tag.contains(NBT_AMOUNT)) return;

        ResourceLocation id = ResourceLocation.tryParse(tag.getString(NBT_FLUID));
        if (id == null) return;
        Fluid loadedFluid = BuiltInRegistries.FLUID.get(id);
        long loadedAmount = Math.min(Math.max(0, tag.getLong(NBT_AMOUNT)), this.capacityInternal);
        if (loadedFluid == Fluids.EMPTY || loadedAmount <= 0 || !isValidFuel(loadedFluid)) return;

        this.fluid = loadedFluid;
        this.amountInternal = loadedAmount;
    }

    public static long mbToInternal(long milliBuckets) {
        return Math.multiplyExact(milliBuckets, FluidResourceSlot.INTERNAL_UNITS_PER_MB);
    }

    public static int internalToMbFloor(long internalUnits) {
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(0, internalUnits / FluidResourceSlot.INTERNAL_UNITS_PER_MB));
    }

    private static boolean isValidFuel(@NotNull Fluid fluid) {
        return fluid != Fluids.EMPTY && fluid.builtInRegistryHolder().is(GCTags.FUEL);
    }
}
