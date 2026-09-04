/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Objects;

/** Forge implementation of the MachineLib 0.2 filters Galacticraft 1.20.1 imports. */
public final class ResourceFilters {
    private static final ResourceFilter<?> ANY = (resource, tag) -> true;
    private static final ResourceFilter<?> NONE = (resource, tag) -> false;

    public static final ResourceFilter<Item> CAN_EXTRACT_ENERGY = (item, tag) -> energy(item, tag, true, false);
    public static final ResourceFilter<Item> CAN_EXTRACT_ENERGY_STRICT = (item, tag) -> energy(item, tag, true, true);
    public static final ResourceFilter<Item> CAN_INSERT_ENERGY = (item, tag) -> energy(item, tag, false, false);
    public static final ResourceFilter<Item> CAN_INSERT_ENERGY_STRICT = (item, tag) -> energy(item, tag, false, true);

    private ResourceFilters() {}

    @SuppressWarnings("unchecked")
    public static <Resource> ResourceFilter<Resource> any() { return (ResourceFilter<Resource>) ANY; }
    @SuppressWarnings("unchecked")
    public static <Resource> ResourceFilter<Resource> none() { return (ResourceFilter<Resource>) NONE; }

    public static <Resource> ResourceFilter<Resource> ofResourceAnyNBT(Resource resource) {
        return (actual, tag) -> actual == resource;
    }

    public static <Resource> ResourceFilter<Resource> ofResource(Resource resource) {
        return (actual, tag) -> actual == resource && empty(tag);
    }

    public static <Resource> ResourceFilter<Resource> ofResource(Resource resource, CompoundTag tag) {
        CompoundTag expected = copy(tag);
        return (actual, actualTag) -> actual == resource && tagsEqual(expected, actualTag);
    }

    public static ResourceFilter<Item> itemTagAnyNBT(TagKey<Item> tag) {
        return (item, nbt) -> item != null && item.builtInRegistryHolder().is(tag);
    }

    public static ResourceFilter<Item> itemTag(TagKey<Item> tag) {
        return (item, nbt) -> item != null && item.builtInRegistryHolder().is(tag) && empty(nbt);
    }

    public static ResourceFilter<Item> itemTag(TagKey<Item> tag, CompoundTag nbt) {
        CompoundTag expected = copy(nbt);
        return (item, actual) -> item != null && item.builtInRegistryHolder().is(tag) && tagsEqual(expected, actual);
    }

    public static ResourceFilter<Fluid> fluidTagAnyNBT(TagKey<Fluid> tag) {
        return (fluid, nbt) -> fluid != null && fluid.builtInRegistryHolder().is(tag);
    }

    public static ResourceFilter<Fluid> fluidTag(TagKey<Fluid> tag) {
        return (fluid, nbt) -> fluid != null && fluid.builtInRegistryHolder().is(tag) && empty(nbt);
    }

    public static ResourceFilter<Fluid> fluidTag(TagKey<Fluid> tag, CompoundTag nbt) {
        CompoundTag expected = copy(nbt);
        return (fluid, actual) -> fluid != null && fluid.builtInRegistryHolder().is(tag) && tagsEqual(expected, actual);
    }

    public static ResourceFilter<Item> isFluidStorage() {
        return (item, nbt) -> item != null && stack(item, nbt).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    public static ResourceFilter<Item> canExtractFluidStrict(Fluid fluid) {
        return (item, nbt) -> {
            if (item == null) return false;
            return stack(item, nbt).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                    .map(handler -> !handler.drain(new FluidStack(fluid, 1), IFluidHandler.FluidAction.SIMULATE).isEmpty())
                    .orElse(false);
        };
    }

    public static ResourceFilter<Item> canExtractFluidStrict(Fluid fluid, CompoundTag fluidTag) {
        return canExtractFluidStrict(fluid);
    }

    public static ResourceFilter<Item> canInsertFluidStrict(Fluid fluid) {
        return (item, nbt) -> {
            if (item == null) return false;
            return stack(item, nbt).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                    .map(handler -> handler.fill(new FluidStack(fluid, 1), IFluidHandler.FluidAction.SIMULATE) > 0)
                    .orElse(false);
        };
    }

    public static ResourceFilter<Item> canInsertFluidStrict(Fluid fluid, CompoundTag fluidTag) {
        return canInsertFluidStrict(fluid);
    }

    private static boolean energy(Item item, CompoundTag tag, boolean extraction, boolean strict) {
        if (item == null) return false;
        return stack(item, tag).getCapability(ForgeCapabilities.ENERGY).map(storage -> {
            if (extraction) return storage.canExtract() && (!strict || storage.getEnergyStored() > 0);
            return storage.canReceive() && (!strict || storage.getEnergyStored() < storage.getMaxEnergyStored());
        }).orElse(false);
    }

    private static ItemStack stack(Item item, CompoundTag tag) {
        ItemStack stack = new ItemStack(item);
        if (!empty(tag)) stack.setTag(tag.copy());
        return stack;
    }

    private static boolean tagsEqual(CompoundTag a, CompoundTag b) {
        return empty(a) && empty(b) || Objects.equals(a, b);
    }
    private static boolean empty(CompoundTag tag) { return tag == null || tag.isEmpty(); }
    private static CompoundTag copy(CompoundTag tag) { return empty(tag) ? null : tag.copy(); }
}
