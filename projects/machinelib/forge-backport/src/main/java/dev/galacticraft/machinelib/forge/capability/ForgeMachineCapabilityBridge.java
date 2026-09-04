/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.forge.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Reusable capability plumbing for the eventual Forge MachineBlockEntity port.
 * A MachineBlockEntity delegates getCapability(...) here and invalidates this
 * bridge from invalidateCaps(). Providers receive the queried side, including
 * null for unsided access, so MachineIOConfig can remain responsible for face rules.
 */
public final class ForgeMachineCapabilityBridge {
    private final Function<Direction, IEnergyStorage> energyProvider;
    private final Function<Direction, IItemHandler> itemProvider;
    private final Function<Direction, IFluidHandler> fluidProvider;

    private final Map<Direction, LazyOptional<IEnergyStorage>> sidedEnergy = new EnumMap<>(Direction.class);
    private final Map<Direction, LazyOptional<IItemHandler>> sidedItems = new EnumMap<>(Direction.class);
    private final Map<Direction, LazyOptional<IFluidHandler>> sidedFluids = new EnumMap<>(Direction.class);
    private LazyOptional<IEnergyStorage> unsidedEnergy;
    private LazyOptional<IItemHandler> unsidedItems;
    private LazyOptional<IFluidHandler> unsidedFluids;

    public ForgeMachineCapabilityBridge(
            Function<Direction, IEnergyStorage> energyProvider,
            Function<Direction, IItemHandler> itemProvider,
            Function<Direction, IFluidHandler> fluidProvider) {
        this.energyProvider = energyProvider;
        this.itemProvider = itemProvider;
        this.fluidProvider = fluidProvider;
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energy(side).cast();
        if (capability == ForgeCapabilities.ITEM_HANDLER) return items(side).cast();
        if (capability == ForgeCapabilities.FLUID_HANDLER) return fluids(side).cast();
        return LazyOptional.empty();
    }

    public void invalidate() {
        if (this.unsidedEnergy != null) this.unsidedEnergy.invalidate();
        if (this.unsidedItems != null) this.unsidedItems.invalidate();
        if (this.unsidedFluids != null) this.unsidedFluids.invalidate();
        this.sidedEnergy.values().forEach(LazyOptional::invalidate);
        this.sidedItems.values().forEach(LazyOptional::invalidate);
        this.sidedFluids.values().forEach(LazyOptional::invalidate);
        this.sidedEnergy.clear();
        this.sidedItems.clear();
        this.sidedFluids.clear();
        this.unsidedEnergy = null;
        this.unsidedItems = null;
        this.unsidedFluids = null;
    }

    private LazyOptional<IEnergyStorage> energy(Direction side) {
        if (side == null) {
            if (this.unsidedEnergy == null) this.unsidedEnergy = optional(this.energyProvider.apply(null));
            return this.unsidedEnergy;
        }
        return this.sidedEnergy.computeIfAbsent(side, key -> optional(this.energyProvider.apply(key)));
    }

    private LazyOptional<IItemHandler> items(Direction side) {
        if (side == null) {
            if (this.unsidedItems == null) this.unsidedItems = optional(this.itemProvider.apply(null));
            return this.unsidedItems;
        }
        return this.sidedItems.computeIfAbsent(side, key -> optional(this.itemProvider.apply(key)));
    }

    private LazyOptional<IFluidHandler> fluids(Direction side) {
        if (side == null) {
            if (this.unsidedFluids == null) this.unsidedFluids = optional(this.fluidProvider.apply(null));
            return this.unsidedFluids;
        }
        return this.sidedFluids.computeIfAbsent(side, key -> optional(this.fluidProvider.apply(key)));
    }

    private static <T> LazyOptional<T> optional(T value) {
        return value == null ? LazyOptional.empty() : LazyOptional.of(() -> value);
    }
}
