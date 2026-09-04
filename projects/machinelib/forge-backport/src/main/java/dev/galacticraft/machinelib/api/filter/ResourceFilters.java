/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.filter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

/** Loader-neutral filters with Forge-backed capability probes where needed. */
public final class ResourceFilters {
    private static final ResourceFilter<?> ANY = (resource, tag) -> true;
    private static final ResourceFilter<?> NONE = (resource, tag) -> false;

    public static final ResourceFilter<Item> CAN_EXTRACT_ENERGY = (item, tag) -> {
        if (item == null) return false;
        ItemStack stack = stack(item, tag);
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(storage -> storage.canExtract() && storage.getEnergyStored() > 0)
                .orElse(false);
    };

    public static final ResourceFilter<Item> CAN_INSERT_ENERGY = (item, tag) -> {
        if (item == null) return false;
        ItemStack stack = stack(item, tag);
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(storage -> storage.canReceive() && storage.getEnergyStored() < storage.getMaxEnergyStored())
                .orElse(false);
    };

    private ResourceFilters() {
    }

    @SuppressWarnings("unchecked")
    public static <Resource> ResourceFilter<Resource> any() {
        return (ResourceFilter<Resource>) ANY;
    }

    @SuppressWarnings("unchecked")
    public static <Resource> ResourceFilter<Resource> none() {
        return (ResourceFilter<Resource>) NONE;
    }

    public static <Resource> ResourceFilter<Resource> ofNBT(CompoundTag tag) {
        CompoundTag expected = copy(tag);
        return (resource, actual) -> tagsEqual(expected, actual);
    }

    public static <Resource> ResourceFilter<Resource> ofResource(Resource resource) {
        return (actual, tag) -> actual == resource;
    }

    public static <Resource> ResourceFilter<Resource> ofResource(Resource resource, CompoundTag tag) {
        CompoundTag expected = copy(tag);
        return (actual, actualTag) -> actual == resource && tagsEqual(expected, actualTag);
    }

    public static ResourceFilter<Item> itemTag(TagKey<Item> tag) {
        return (item, nbt) -> item != null && item.builtInRegistryHolder().is(tag);
    }

    public static ResourceFilter<Item> itemTag(TagKey<Item> tag, CompoundTag nbt) {
        CompoundTag expected = copy(nbt);
        return (item, actual) -> item != null && item.builtInRegistryHolder().is(tag) && tagsEqual(expected, actual);
    }

    public static ResourceFilter<Fluid> fluidTag(TagKey<Fluid> tag) {
        return (fluid, nbt) -> fluid != null && fluid.builtInRegistryHolder().is(tag);
    }

    public static ResourceFilter<Fluid> fluidTag(TagKey<Fluid> tag, CompoundTag nbt) {
        CompoundTag expected = copy(nbt);
        return (fluid, actual) -> fluid != null && fluid.builtInRegistryHolder().is(tag) && tagsEqual(expected, actual);
    }

    public static ResourceFilter<Item> canExtractFluid(Fluid fluid) {
        return (item, tag) -> {
            if (item == null) return false;
            ItemStack stack = stack(item, tag);
            return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                    .map(handler -> !handler.drain(new FluidStack(fluid, 1), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE).isEmpty())
                    .orElse(false);
        };
    }

    public static ResourceFilter<Item> canInsertFluid(Fluid fluid) {
        return (item, tag) -> {
            if (item == null) return false;
            ItemStack stack = stack(item, tag);
            return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                    .map(handler -> handler.fill(new FluidStack(fluid, 1), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE) > 0)
                    .orElse(false);
        };
    }

    public static boolean tagsEqual(CompoundTag first, CompoundTag second) {
        boolean firstEmpty = first == null || first.isEmpty();
        boolean secondEmpty = second == null || second.isEmpty();
        return firstEmpty && secondEmpty || Objects.equals(first, second);
    }

    private static CompoundTag copy(CompoundTag tag) {
        return tag == null || tag.isEmpty() ? null : tag.copy();
    }

    private static ItemStack stack(Item item, CompoundTag tag) {
        ItemStack stack = new ItemStack(item);
        if (tag != null && !tag.isEmpty()) stack.setTag(tag.copy());
        return stack;
    }
}
