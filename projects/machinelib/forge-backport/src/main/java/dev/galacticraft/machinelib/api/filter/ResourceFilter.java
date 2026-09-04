/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.filter;

import net.minecraft.nbt.CompoundTag;

/** Filters a resource by identity/type and optional NBT. */
@FunctionalInterface
public interface ResourceFilter<Resource> {
    boolean test(Resource resource, CompoundTag tag);
}
