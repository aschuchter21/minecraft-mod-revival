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

package dev.galacticraft.mod.api.wire.impl;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.api.wire.Wire;
import dev.galacticraft.mod.api.wire.WireNetwork;
import dev.galacticraft.mod.util.DirectionUtil;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

/** Forge Energy implementation of the Galacticraft 1.20.1 wire network. */
public class WireNetworkImpl implements WireNetwork {
    private final @NotNull ServerLevel world;
    private final @NotNull Object2ObjectOpenHashMap<BlockPos, IEnergyStorage> storages = new Object2ObjectOpenHashMap<>();
    private final @NotNull ObjectSet<BlockPos> wires = new ObjectLinkedOpenHashSet<>(1);
    private final @NotNull ObjectSet<WireNetwork> peerNetworks = new ObjectLinkedOpenHashSet<>(0);
    private boolean markedForRemoval = false;
    private final long maxTransferRate;
    private long tickId;
    private long transferred = 0;

    public WireNetworkImpl(@NotNull ServerLevel world, long maxTransferRate) {
        this.world = world;
        this.maxTransferRate = maxTransferRate;
        this.tickId = world.getServer().getTickCount();
    }

    private static int saturate(long value) {
        return (int) Math.min(Math.max(value, 0), Integer.MAX_VALUE);
    }

    private @Nullable IEnergyStorage findStorage(BlockPos pos, Direction side) {
        BlockEntity blockEntity = this.world.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved()) return null;
        return blockEntity.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
    }

    @Override
    public boolean addWire(@NotNull BlockPos pos, @Nullable Wire wire) {
        if (this.markedForRemoval()) return false;
        if (wire == null) wire = (Wire) world.getBlockEntity(pos);
        if (wire == null) throw new IllegalStateException("Attempted to add wire that does not exist at " + pos);
        if (!this.isCompatibleWith(wire)) return false;

        wire.setNetwork(this);
        this.wires.add(pos);
        for (Direction direction : Constant.Misc.DIRECTIONS) {
            if (!wire.canConnect(direction)) continue;
            BlockPos adjacentPos = pos.relative(direction);
            BlockEntity blockEntity = world.getBlockEntity(adjacentPos);
            if (blockEntity instanceof Wire adjacentWire && !blockEntity.isRemoved()) {
                if (adjacentWire.canConnect(direction.getOpposite())) {
                    if (this.isCompatibleWith(adjacentWire)) {
                        if (adjacentWire.getNetwork() == null || adjacentWire.getNetwork().markedForRemoval()) {
                            this.addWire(adjacentPos, adjacentWire);
                        } else if (adjacentWire.getNetwork() != this) {
                            this.takeAll(adjacentWire.getNetwork());
                        }
                    } else {
                        this.peerNetworks.add(adjacentWire.getOrCreateNetwork());
                    }
                }
                continue;
            }

            IEnergyStorage storage = this.findStorage(adjacentPos, direction.getOpposite());
            if (storage != null && storage.canReceive()) this.storages.put(adjacentPos, storage);
        }
        return true;
    }

    public void takeAll(@NotNull WireNetwork network) {
        for (BlockPos pos : network.getAllWires()) {
            BlockEntity entity = this.world.getBlockEntity(pos);
            if (entity instanceof Wire wire && !entity.isRemoved()) {
                wire.setNetwork(this);
                this.wires.add(pos);
            }
        }
        this.storages.putAll(network.getStorages());
        network.markForRemoval();
    }

    @Override
    public void removeWire(Wire wire, @NotNull BlockPos removedPos) {
        if (this.markedForRemoval()) {
            this.wires.clear();
            Constant.LOGGER.warn("Tried to remove wire from removed network!");
            return;
        }
        this.wires.remove(removedPos);
        if (this.wires.isEmpty()) {
            this.markForRemoval();
            return;
        }

        List<BlockPos> adjacent = new LinkedList<>();
        this.reattachAdjacent(removedPos, this.storages,
                (blockPos, direction) -> this.findStorage(blockPos.relative(direction), direction.getOpposite()), adjacent);
        adjacent.clear();

        for (Direction direction : Constant.Misc.DIRECTIONS) {
            if (!wire.canConnect(direction)) continue;
            BlockPos adjacentWirePos = removedPos.relative(direction);
            if (this.wires.contains(adjacentWirePos)
                    && this.world.getBlockEntity(adjacentWirePos) instanceof Wire adjacentWire
                    && adjacentWire.canConnect(direction.getOpposite())) {
                adjacent.add(adjacentWirePos);
            }
        }

        List<List<BlockPos>> mappedWires = new LinkedList<>();
        for (BlockPos blockPos : adjacent) {
            boolean handled = false;
            for (List<BlockPos> mapped : mappedWires) {
                if (mapped.contains(blockPos)) {
                    handled = true;
                    break;
                }
            }
            if (handled) continue;
            List<BlockPos> mapped = new LinkedList<>();
            mapped.add(blockPos);
            this.traverse(mapped, blockPos, null);
            mappedWires.add(mapped);
        }

        if (mappedWires.size() <= 1) return;
        this.markForRemoval();
        for (List<BlockPos> positions : mappedWires) {
            WireNetwork network = WireNetwork.create(this.world, this.getMaxTransferRate());
            network.addWire(positions.get(0), null);
        }
    }

    private void traverse(List<BlockPos> list, BlockPos pos, @Nullable Direction ignore) {
        for (Direction direction : Constant.Misc.DIRECTIONS) {
            if (direction.getOpposite() == ignore) continue;
            if (!(world.getBlockEntity(pos) instanceof Wire wire) || !wire.canConnect(direction)) continue;
            BlockPos adjacentPos = pos.relative(direction);
            if (this.wires.contains(adjacentPos)
                    && world.getBlockEntity(adjacentPos) instanceof Wire adjacentWire
                    && adjacentWire.canConnect(direction.getOpposite())
                    && !list.contains(adjacentPos)) {
                list.add(adjacentPos);
                this.traverse(list, adjacentPos, direction);
            }
        }
    }

    private <T> void reattachAdjacent(BlockPos pos, Object2ObjectOpenHashMap<BlockPos, T> map,
                                      BiFunction<BlockPos, Direction, T> function, List<BlockPos> optionalList) {
        for (Direction direction : Constant.Misc.DIRECTIONS) {
            BlockPos adjacentPos = pos.relative(direction);
            if (map.remove(adjacentPos) == null) continue;
            for (Direction dir : Constant.Misc.DIRECTIONS) {
                if (dir == direction.getOpposite()) continue;
                BlockPos wirePos = adjacentPos.relative(dir);
                if (this.wires.contains(wirePos)
                        && world.getBlockEntity(wirePos) instanceof Wire wire
                        && wire.canConnect(dir.getOpposite())) {
                    T value = function.apply(adjacentPos, dir);
                    if (value != null) {
                        optionalList.add(adjacentPos);
                        map.put(adjacentPos, value);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean updateConnection(@NotNull BlockPos adjacentToUpdated, @NotNull BlockPos updatedPos) {
        if (world.getBlockEntity(updatedPos) instanceof Wire) return false;
        this.storages.remove(updatedPos);
        BlockPos vector = updatedPos.subtract(adjacentToUpdated);
        Direction direction = DirectionUtil.fromNormal(vector.getX(), vector.getY(), vector.getZ());
        IEnergyStorage storage = this.findStorage(updatedPos, direction.getOpposite());
        if (storage != null && storage.canReceive()) {
            this.storages.put(updatedPos, storage);
            return true;
        }
        return false;
    }

    @Override
    public long insert(@NotNull BlockPos fromWire, long amount, Direction direction, boolean simulate) {
        if (amount <= 0) return 0;
        BlockPos source = fromWire.relative(direction);
        resetTickIfNeeded();
        long permitted = Math.min(amount, this.getMaxTransferRate() - this.transferred);
        if (permitted <= 0) return 0;

        Object2LongArrayMap<WireNetwork> nonFullInsertables = new Object2LongArrayMap<>(1 + this.peerNetworks.size());
        nonFullInsertables.defaultReturnValue(-1);
        this.getNonFullInsertables(nonFullInsertables, source, permitted);

        long requested = 0;
        LongIterator it = nonFullInsertables.values().longIterator();
        while (it.hasNext()) requested += it.nextLong();
        if (requested <= 0) return 0;

        double ratio = Math.min(1.0, (double) permitted / (double) requested);
        long available = permitted;
        for (WireNetwork network : nonFullInsertables.keySet()) {
            available = network.insertInternal(permitted, ratio, available, simulate);
            if (available <= 0) break;
        }
        return permitted - Math.max(available, 0);
    }

    @Override
    public long insertInternal(long amount, double ratio, long available, boolean simulate) {
        resetTickIfNeeded();
        long allowedHere = Math.min(amount, this.maxTransferRate - this.transferred);
        if (allowedHere <= 0) return available;

        for (IEnergyStorage storage : this.storages.values()) {
            long offer = Math.min(Math.min(available, (long) (amount * ratio)), allowedHere);
            if (offer <= 0) continue;
            int accepted = storage.receiveEnergy(saturate(offer), simulate);
            available -= accepted;
            allowedHere -= accepted;
            if (!simulate) this.transferred += accepted;
            if (available <= 0 || allowedHere <= 0) break;
        }
        return available;
    }

    @Override
    public void getNonFullInsertables(Object2LongMap<WireNetwork> energyRequirement, BlockPos source, long amount) {
        resetTickIfNeeded();
        amount = Math.min(amount, this.maxTransferRate - this.transferred);
        if (amount <= 0) return;

        if (energyRequirement.putIfAbsent(this, 0) == -1) {
            long requested = 0;
            for (Map.Entry<BlockPos, IEnergyStorage> entry : this.storages.entrySet()) {
                if (entry.getKey().equals(source)) continue;
                IEnergyStorage storage = entry.getValue();
                if (storage.canReceive()) requested += storage.receiveEnergy(saturate(amount), true);
            }
            for (WireNetwork peerNetwork : this.peerNetworks) {
                if (!energyRequirement.containsKey(peerNetwork)) {
                    peerNetwork.getNonFullInsertables(energyRequirement, source, amount);
                }
            }
            energyRequirement.put(this, requested);
        }
    }

    private void resetTickIfNeeded() {
        long currentTick = this.world.getServer().getTickCount();
        if (this.tickId != currentTick) {
            this.tickId = currentTick;
            this.transferred = 0;
        }
    }

    @Override public long getMaxTransferRate() { return this.maxTransferRate; }
    @Override public Collection<BlockPos> getAllWires() { return this.wires; }
    @Override public @NotNull Object2ObjectOpenHashMap<BlockPos, IEnergyStorage> getStorages() { return this.storages; }
    @Override public boolean markedForRemoval() { return this.markedForRemoval; }
    @Override public void markForRemoval() { this.markedForRemoval = true; }
    @Override public boolean isCompatibleWith(Wire wire) { return this.getMaxTransferRate() == wire.getMaxTransferRate(); }

    @Override
    public String toString() {
        return "WireNetworkImpl{" +
                "world=" + world.dimension().location() +
                ", insertable=" + storages +
                ", wires=" + wires +
                ", markedForRemoval=" + markedForRemoval +
                ", maxTransferRate=" + maxTransferRate +
                ", tickId=" + tickId +
                ", transferred=" + transferred +
                '}';
    }
}
