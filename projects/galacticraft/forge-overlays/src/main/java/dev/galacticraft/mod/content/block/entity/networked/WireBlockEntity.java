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

package dev.galacticraft.mod.content.block.entity.networked;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.api.wire.Wire;
import dev.galacticraft.mod.api.wire.WireNetwork;
import dev.galacticraft.mod.attribute.energy.WireEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 wire block entity. */
public class WireBlockEntity extends BlockEntity implements Wire {
    @Nullable private WireNetwork network;
    @Nullable private WireEnergyStorage[] insertables;
    @Nullable private LazyOptional<IEnergyStorage>[] energyCapabilities;
    private final int maxTransferRate;
    private final boolean[] connections = new boolean[6];

    public WireBlockEntity(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state, int maxTransferRate) {
        super(type, pos, state);
        this.maxTransferRate = maxTransferRate;
    }

    public static WireBlockEntity createT1(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state) {
        return new WireBlockEntity(type, pos, state, 240);
    }

    public static WireBlockEntity createT2(BlockEntityType<? extends WireBlockEntity> type, BlockPos pos, BlockState state) {
        return new WireBlockEntity(type, pos, state, 480);
    }

    @Override
    public void setNetwork(@Nullable WireNetwork network) {
        this.network = network;
        for (WireEnergyStorage insertable : this.getInsertables()) insertable.setNetwork(network);
    }

    @Override
    public @NotNull WireNetwork getOrCreateNetwork() {
        if (this.network == null || this.network.markedForRemoval()) {
            if (this.level != null && !this.level.isClientSide()) {
                for (Direction direction : Constant.Misc.DIRECTIONS) {
                    if (!this.canConnect(direction)) continue;
                    BlockEntity entity = this.level.getBlockEntity(this.getBlockPos().relative(direction));
                    if (entity instanceof Wire wire && wire.getNetwork() != null
                            && wire.canConnect(direction.getOpposite())
                            && wire.getOrCreateNetwork().isCompatibleWith(this)) {
                        wire.getNetwork().addWire(this.getBlockPos(), this);
                    }
                }
                if (this.network == null || this.network.markedForRemoval()) {
                    this.setNetwork(WireNetwork.create((ServerLevel) this.level, this.getMaxTransferRate()));
                    this.network.addWire(this.getBlockPos(), this);
                }
            }
        }
        return this.network;
    }

    @Override
    @Nullable
    public WireNetwork getNetwork() {
        return this.network;
    }

    public WireEnergyStorage[] getInsertables() {
        if (this.insertables == null) {
            this.insertables = new WireEnergyStorage[6];
            for (Direction direction : Constant.Misc.DIRECTIONS) {
                this.insertables[direction.ordinal()] = new WireEnergyStorage(direction, this.getMaxTransferRate(), this.getBlockPos());
                this.insertables[direction.ordinal()].setNetwork(this.network);
            }
        }
        return this.insertables;
    }

    @SuppressWarnings("unchecked")
    private LazyOptional<IEnergyStorage>[] getEnergyCapabilities() {
        if (this.energyCapabilities == null) {
            this.energyCapabilities = (LazyOptional<IEnergyStorage>[]) new LazyOptional<?>[6];
            for (Direction direction : Constant.Misc.DIRECTIONS) {
                final int index = direction.ordinal();
                this.energyCapabilities[index] = LazyOptional.of(() -> this.getInsertables()[index]);
            }
        }
        return this.energyCapabilities;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            Direction resolvedSide = side == null ? Direction.UP : side;
            return this.getEnergyCapabilities()[resolvedSide.ordinal()].cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (this.energyCapabilities != null) {
            for (LazyOptional<IEnergyStorage> capability : this.energyCapabilities) {
                if (capability != null) capability.invalidate();
            }
            this.energyCapabilities = null;
        }
    }

    @Override
    public int getMaxTransferRate() {
        return this.maxTransferRate;
    }

    @Override
    public void setRemoved() {
        if (this.getNetwork() != null && !this.getNetwork().markedForRemoval()) {
            this.getNetwork().removeWire(this, this.getBlockPos());
        }
        super.setRemoved();
    }

    @Override
    public boolean[] getConnections() {
        return this.connections;
    }

    @Override
    public void calculateConnections() {
        if (this.level == null) return;
        for (Direction direction : Constant.Misc.DIRECTIONS) {
            BlockEntity adjacent = this.level.getBlockEntity(this.getBlockPos().relative(direction));
            this.getConnections()[direction.ordinal()] = this.canConnect(direction)
                    && adjacent != null
                    && adjacent.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).isPresent();
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        this.writeConnectionNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.readConnectionNbt(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
