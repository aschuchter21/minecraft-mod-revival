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

package dev.galacticraft.mod.api.pipe.impl;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.api.pipe.Pipe;
import dev.galacticraft.mod.api.pipe.PipeNetwork;
import dev.galacticraft.mod.util.DirectionUtil;
import dev.galacticraft.mod.util.FluidUtil;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/** Forge fluid implementation of Galacticraft's 1.20.1 pipe graph. */
public class PipeNetworkImpl implements PipeNetwork {
    private final @NotNull ServerLevel world;
    private final @NotNull Object2ObjectOpenHashMap<BlockPos, IFluidHandler> insertable = new Object2ObjectOpenHashMap<>();
    private final @NotNull ObjectSet<BlockPos> pipes = new ObjectLinkedOpenHashSet<>(1);
    private final @NotNull ObjectSet<PipeNetwork> peerNetworks = new ObjectLinkedOpenHashSet<>(0);
    private boolean markedForRemoval;
    private final long maxTransferRate;
    private int tickId;
    private long transferred;
    private @Nullable PipeNetwork.FluidStack fluidTransferred;
    private boolean activeTransfer;

    public PipeNetworkImpl(@NotNull ServerLevel world, long maxTransferRate) {
        this.world = world;
        this.maxTransferRate = maxTransferRate;
        this.tickId = world.getServer().getTickCount();
    }

    private @Nullable IFluidHandler findStorage(BlockPos pos, Direction side) {
        BlockEntity blockEntity = this.world.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved()) return null;
        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
    }

    @Override
    public boolean addPipe(@NotNull BlockPos pos, @Nullable Pipe pipe) {
        if (this.markedForRemoval()) return false;
        if (pipe == null && this.world.getBlockEntity(pos) instanceof Pipe found) pipe = found;
        if (pipe == null || !this.isCompatibleWith(pipe)) return false;

        pipe.setNetwork(this);
        this.pipes.add(pos);
        for (Direction direction : Constant.Misc.DIRECTIONS) {
            if (!pipe.canConnect(direction)) continue;
            BlockPos adjacentPos = pos.relative(direction);
            BlockEntity blockEntity = this.world.getBlockEntity(adjacentPos);
            if (blockEntity instanceof Pipe adjacentPipe && !blockEntity.isRemoved()) {
                if (adjacentPipe.canConnect(direction.getOpposite())) {
                    if (this.isCompatibleWith(adjacentPipe)) {
                        if (adjacentPipe.getNetwork() == null || adjacentPipe.getNetwork().markedForRemoval()) {
                            this.addPipe(adjacentPos, adjacentPipe);
                        } else if (adjacentPipe.getNetwork() != this) {
                            this.takeAll(adjacentPipe.getNetwork());
                        }
                    } else {
                        this.peerNetworks.add(adjacentPipe.getOrCreateNetwork());
                    }
                }
                continue;
            }

            IFluidHandler handler = this.findStorage(adjacentPos, direction.getOpposite());
            if (handler != null) this.insertable.put(adjacentPos, handler);
        }
        return true;
    }

    public void takeAll(@NotNull PipeNetwork network) {
        for (BlockPos pos : network.getAllPipes()) {
            BlockEntity entity = this.world.getBlockEntity(pos);
            if (entity instanceof Pipe pipe && !entity.isRemoved()) {
                pipe.setNetwork(this);
                this.pipes.add(pos);
            }
        }
        this.insertable.putAll(network.getInsertable());
        network.markForRemoval();
    }

    @Override
    public void removePipe(Pipe pipe, @NotNull BlockPos removedPos) {
        if (this.markedForRemoval()) {
            this.pipes.clear();
            Constant.LOGGER.warn("Tried to remove pipe from removed network!");
            return;
        }
        this.pipes.remove(removedPos);
        if (this.pipes.isEmpty()) {
            this.markForRemoval();
            return;
        }

        List<BlockPos> adjacent = new LinkedList<>();
        this.reattachAdjacent(removedPos, this.insertable,
                (blockPos, direction) -> this.findStorage(blockPos.relative(direction), direction.getOpposite()), adjacent);
        adjacent.clear();

        for (Direction direction : Constant.Misc.DIRECTIONS) {
            if (!pipe.canConnect(direction)) continue;
            BlockPos adjacentPipePos = removedPos.relative(direction);
            if (this.pipes.contains(adjacentPipePos)
                    && this.world.getBlockEntity(adjacentPipePos) instanceof Pipe adjacentPipe
                    && adjacentPipe.canConnect(direction.getOpposite())) {
                adjacent.add(adjacentPipePos);
            }
        }

        List<List<BlockPos>> mappedPipes = new LinkedList<>();
        for (BlockPos blockPos : adjacent) {
            boolean handled = false;
            for (List<BlockPos> mapped : mappedPipes) {
                if (mapped.contains(blockPos)) {
                    handled = true;
                    break;
                }
            }
            if (handled) continue;
            List<BlockPos> mapped = new LinkedList<>();
            mapped.add(blockPos);
            this.traverse(mapped, blockPos, null);
            mappedPipes.add(mapped);
        }

        if (mappedPipes.size() <= 1) return;
        this.markForRemoval();
        for (List<BlockPos> positions : mappedPipes) {
            PipeNetwork network = PipeNetwork.create(this.world, this.getMaxTransferRate());
            network.addPipe(positions.get(0), null);
        }
    }

    private void traverse(List<BlockPos> list, BlockPos pos, @Nullable Direction ignore) {
        for (Direction direction : Constant.Misc.DIRECTIONS) {
            if (direction.getOpposite() == ignore) continue;
            if (!(this.world.getBlockEntity(pos) instanceof Pipe pipe) || !pipe.canConnect(direction)) continue;
            BlockPos adjacentPos = pos.relative(direction);
            if (this.pipes.contains(adjacentPos)
                    && this.world.getBlockEntity(adjacentPos) instanceof Pipe adjacentPipe
                    && adjacentPipe.canConnect(direction.getOpposite())
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
                BlockPos pipePos = adjacentPos.relative(dir);
                if (this.pipes.contains(pipePos)
                        && this.world.getBlockEntity(pipePos) instanceof Pipe pipe
                        && pipe.canConnect(dir.getOpposite())) {
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
        if (this.world.getBlockEntity(updatedPos) instanceof Pipe) return false;
        this.insertable.remove(updatedPos);
        BlockPos vector = updatedPos.subtract(adjacentToUpdated);
        Direction direction = DirectionUtil.fromNormal(vector.getX(), vector.getY(), vector.getZ());
        IFluidHandler handler = this.findStorage(updatedPos, direction.getOpposite());
        if (handler != null) {
            this.insertable.put(updatedPos, handler);
            return true;
        }
        return false;
    }

    @Override
    public long insert(@NotNull BlockPos fromPipe, @NotNull PipeNetwork.FluidStack stack,
                       @NotNull Direction direction, boolean simulate) {
        if (stack.isEmpty() || this.activeTransfer) return 0;
        this.resetTickIfNeeded();
        if (this.fluidTransferred != null && !this.fluidTransferred.sameVariant(stack)) return 0;

        long permitted = Math.min(stack.amount(), this.maxTransferRate - this.transferred);
        permitted = alignToForgeUnits(permitted);
        if (permitted <= 0) return 0;

        BlockPos source = fromPipe.relative(direction.getOpposite());
        Object2LongArrayMap<PipeNetwork> nonFullInsertables = new Object2LongArrayMap<>(1 + this.peerNetworks.size());
        nonFullInsertables.defaultReturnValue(-1);
        this.getNonFullInsertables(nonFullInsertables, source,
                PipeNetwork.FluidStack.create(stack.fluid(), stack.tag(), permitted));

        long requested = 0;
        for (long amount : nonFullInsertables.values()) requested += amount;
        if (requested <= 0) return 0;

        double ratio = Math.min(1.0, (double) permitted / (double) requested);
        long available = permitted;
        this.activeTransfer = true;
        try {
            for (PipeNetwork network : nonFullInsertables.keySet()) {
                available = network.insertInternal(stack, ratio, available, simulate);
                if (available <= 0) break;
            }
        } finally {
            this.activeTransfer = false;
        }
        long accepted = permitted - Math.max(0, available);
        if (!simulate && accepted > 0 && this.fluidTransferred == null) {
            this.fluidTransferred = PipeNetwork.FluidStack.create(stack.fluid(), stack.tag(), 1);
        }
        return accepted;
    }

    @Override
    public long insertInternal(@NotNull PipeNetwork.FluidStack amount, double ratio, long available, boolean simulate) {
        this.resetTickIfNeeded();
        if (this.fluidTransferred != null && !this.fluidTransferred.sameVariant(amount)) return available;

        long allowedHere = alignToForgeUnits(Math.min(amount.amount(), this.maxTransferRate - this.transferred));
        if (allowedHere <= 0) return available;
        IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;

        for (IFluidHandler handler : this.insertable.values()) {
            long offer = Math.min(Math.min(available, (long) (amount.amount() * ratio)), allowedHere);
            offer = alignToForgeUnits(offer);
            if (offer <= 0) continue;
            int offerMb = toForgeMb(offer);
            int acceptedMb = handler.fill(amount.toForgeStack(offerMb), action);
            long accepted = acceptedMb * FluidUtil.DROPLETS_PER_MILLIBUCKET;
            if (accepted <= 0) continue;
            available -= accepted;
            allowedHere -= accepted;
            if (!simulate) {
                this.transferred += accepted;
                if (this.fluidTransferred == null) {
                    this.fluidTransferred = PipeNetwork.FluidStack.create(amount.fluid(), amount.tag(), 1);
                }
            }
            if (available <= 0 || allowedHere <= 0) break;
        }
        return available;
    }

    @Override
    public void getNonFullInsertables(Object2LongMap<PipeNetwork> fluidRequirement, BlockPos source,
                                      @NotNull PipeNetwork.FluidStack amount) {
        this.resetTickIfNeeded();
        if (this.fluidTransferred != null && !this.fluidTransferred.sameVariant(amount)) {
            fluidRequirement.putIfAbsent(this, 0);
            return;
        }

        long permitted = alignToForgeUnits(Math.min(amount.amount(), this.maxTransferRate - this.transferred));
        if (permitted <= 0) return;
        if (fluidRequirement.putIfAbsent(this, 0) == -1) {
            long requested = 0;
            for (Map.Entry<BlockPos, IFluidHandler> entry : this.insertable.entrySet()) {
                if (entry.getKey().equals(source)) continue;
                int acceptedMb = entry.getValue().fill(amount.toForgeStack(toForgeMb(permitted)),
                        IFluidHandler.FluidAction.SIMULATE);
                requested += acceptedMb * FluidUtil.DROPLETS_PER_MILLIBUCKET;
                if (requested >= permitted) {
                    requested = permitted;
                    break;
                }
            }
            for (PipeNetwork peerNetwork : this.peerNetworks) {
                if (!fluidRequirement.containsKey(peerNetwork)) {
                    peerNetwork.getNonFullInsertables(fluidRequirement, source, amount);
                }
            }
            fluidRequirement.put(this, requested);
        }
    }

    private void resetTickIfNeeded() {
        int currentTick = this.world.getServer().getTickCount();
        if (this.tickId != currentTick) {
            this.tickId = currentTick;
            this.transferred = 0;
            this.fluidTransferred = null;
        }
    }

    private static long alignToForgeUnits(long droplets) {
        return Math.max(0, droplets / FluidUtil.DROPLETS_PER_MILLIBUCKET * FluidUtil.DROPLETS_PER_MILLIBUCKET);
    }

    private static int toForgeMb(long droplets) {
        return (int) Math.min(Integer.MAX_VALUE, droplets / FluidUtil.DROPLETS_PER_MILLIBUCKET);
    }

    @Override public long getMaxTransferRate() { return this.maxTransferRate; }
    @Override public Collection<BlockPos> getAllPipes() { return this.pipes; }
    @Override public @NotNull Object2ObjectOpenHashMap<BlockPos, IFluidHandler> getInsertable() { return this.insertable; }
    @Override public boolean markedForRemoval() { return this.markedForRemoval; }
    @Override public void markForRemoval() { this.markedForRemoval = true; }
    @Override public boolean isCompatibleWith(Pipe pipe) { return this.getMaxTransferRate() == pipe.getMaxTransferRate(); }

    @Override
    public String toString() {
        return "PipeNetworkImpl{" +
                "world=" + world.dimension().location() +
                ", insertable=" + insertable +
                ", pipes=" + pipes +
                ", markedForRemoval=" + markedForRemoval +
                ", maxTransferRate=" + maxTransferRate +
                ", tickId=" + tickId +
                ", transferred=" + transferred +
                '}';
    }
}
