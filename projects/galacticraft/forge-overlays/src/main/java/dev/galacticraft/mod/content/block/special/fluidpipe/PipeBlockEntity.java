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

package dev.galacticraft.mod.content.block.special.fluidpipe;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.api.pipe.Pipe;
import dev.galacticraft.mod.api.pipe.PipeNetwork;
import dev.galacticraft.mod.attribute.fluid.PipeFluidInsertable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 pipe block entity exposing a sided native fluid capability. */
public abstract class PipeBlockEntity extends BlockEntity implements Pipe {
    private @NotNull PipeFluidInsertable @Nullable [] insertables;
    private @Nullable LazyOptional<IFluidHandler>[] fluidCapabilities;
    private @Nullable PipeNetwork network;
    private DyeColor color = DyeColor.WHITE;
    private final long maxTransferRate;
    private final boolean[] connections = new boolean[6];

    public PipeBlockEntity(BlockEntityType<? extends PipeBlockEntity> type, BlockPos pos,
                           BlockState state, long maxTransferRate) {
        super(type, pos, state);
        this.maxTransferRate = maxTransferRate;
    }

    @Override
    public void setNetwork(@Nullable PipeNetwork network) {
        this.network = network;
        for (PipeFluidInsertable insertable : this.getInsertables()) insertable.setNetwork(network);
    }

    @Override
    public @NotNull PipeNetwork getOrCreateNetwork() {
        if (this.network == null || this.network.markedForRemoval()) {
            if (this.level != null && !this.level.isClientSide()) {
                for (Direction direction : Constant.Misc.DIRECTIONS) {
                    if (!this.canConnect(direction)) continue;
                    BlockEntity entity = this.level.getBlockEntity(this.worldPosition.relative(direction));
                    if (entity instanceof Pipe pipe && pipe.getNetwork() != null
                            && pipe.canConnect(direction.getOpposite())
                            && pipe.getOrCreateNetwork().isCompatibleWith(this)) {
                        pipe.getNetwork().addPipe(this.worldPosition, this);
                    }
                }
                if (this.network == null || this.network.markedForRemoval()) {
                    this.setNetwork(PipeNetwork.create((ServerLevel) this.level, this.getMaxTransferRate()));
                    this.network.addPipe(this.worldPosition, this);
                }
            }
        }
        return this.network;
    }

    @Override
    public @Nullable PipeNetwork getNetwork() {
        return this.network;
    }

    public @NotNull PipeFluidInsertable[] getInsertables() {
        if (this.insertables == null) {
            this.insertables = new PipeFluidInsertable[6];
            for (Direction direction : Constant.Misc.DIRECTIONS) {
                this.insertables[direction.ordinal()] = new PipeFluidInsertable(
                        direction, this.getMaxTransferRate(), this.worldPosition);
                this.insertables[direction.ordinal()].setNetwork(this.network);
            }
        }
        return this.insertables;
    }

    @SuppressWarnings("unchecked")
    private LazyOptional<IFluidHandler>[] getFluidCapabilities() {
        if (this.fluidCapabilities == null) {
            this.fluidCapabilities = (LazyOptional<IFluidHandler>[]) new LazyOptional<?>[6];
            for (Direction direction : Constant.Misc.DIRECTIONS) {
                int index = direction.ordinal();
                this.fluidCapabilities[index] = LazyOptional.of(() -> this.getInsertables()[index]);
            }
        }
        return this.fluidCapabilities;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            Direction resolvedSide = side == null ? Direction.UP : side;
            return this.getFluidCapabilities()[resolvedSide.ordinal()].cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (this.fluidCapabilities != null) {
            for (LazyOptional<IFluidHandler> capability : this.fluidCapabilities) {
                if (capability != null) capability.invalidate();
            }
            this.fluidCapabilities = null;
        }
    }

    @Override public long getMaxTransferRate() { return this.maxTransferRate; }
    @Override public boolean[] getConnections() { return this.connections; }
    @Override public DyeColor getColor() { return this.color; }
    @Override public void setColor(DyeColor color) { this.color = color; }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.readColorNbt(nbt);
        this.readConnectionNbt(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        this.writeColorNbt(nbt);
        this.writeConnectionNbt(nbt);
    }

    @Override
    public void setRemoved() {
        if (this.getNetwork() != null && !this.getNetwork().markedForRemoval()) {
            this.getNetwork().removePipe(this, this.worldPosition);
        }
        super.setRemoved();
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return this.saveWithoutMetadata(); }
}
