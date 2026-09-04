/*
 * Copyright (c) 2019-2023 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package dev.galacticraft.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

/** Forge 1.20.1 fluid helpers using Galacticraft's historical 81 droplets per mB scale. */
public final class FluidUtil {
    public static final long DROPLETS_PER_MILLIBUCKET = 81L;
    public static final long DROPLETS_PER_BUCKET = 81_000L;
    public static final String SUFFIX_MILLIBUCKETS = "mB";
    public static final String SUFFIX_BUCKETS = "B";

    private FluidUtil() {}

    public static long bucketsToDroplets(int buckets) {
        return buckets * DROPLETS_PER_BUCKET;
    }

    public static boolean canAccessFluid(Level world, BlockPos offset, @NotNull Direction direction) {
        BlockEntity blockEntity = world.getBlockEntity(offset);
        return blockEntity != null
                && blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent();
    }
}
