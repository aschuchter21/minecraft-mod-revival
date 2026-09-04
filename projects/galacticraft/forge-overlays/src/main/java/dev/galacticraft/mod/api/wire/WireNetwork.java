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

package dev.galacticraft.mod.api.wire;

import dev.galacticraft.mod.api.wire.impl.WireNetworkImpl;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/** Forge 1.20.1 wire-network contract. Internal energy remains long-valued. */
public interface WireNetwork {
    static WireNetwork create(ServerLevel world, long maxTransferRate) {
        return new WireNetworkImpl(world, maxTransferRate);
    }

    boolean addWire(@NotNull BlockPos pos, @Nullable Wire wire);
    void removeWire(Wire wire, @NotNull BlockPos removedPos);
    boolean updateConnection(@NotNull BlockPos adjacentToUpdated, @NotNull BlockPos updatedPos);

    /** @return amount accepted by the network. */
    long insert(@NotNull BlockPos fromWire, long amount, Direction direction, boolean simulate);

    long insertInternal(long amount, double ratio, long available, boolean simulate);
    void getNonFullInsertables(Object2LongMap<WireNetwork> energyRequirement, BlockPos source, long amount);

    long getMaxTransferRate();
    Collection<BlockPos> getAllWires();
    Map<BlockPos, IEnergyStorage> getStorages();
    boolean markedForRemoval();
    void markForRemoval();
    boolean isCompatibleWith(Wire wire);
}
