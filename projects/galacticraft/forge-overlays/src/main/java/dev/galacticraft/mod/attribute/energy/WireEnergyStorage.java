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

package dev.galacticraft.mod.attribute.energy;

import dev.galacticraft.mod.api.wire.WireNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/** Forge Energy endpoint for one side of a Galacticraft wire. */
public class WireEnergyStorage implements IEnergyStorage {
    private final Direction direction;
    private final int transferRate;
    private final BlockPos pos;
    private @Nullable WireNetwork network;

    public WireEnergyStorage(Direction direction, int transferRate, BlockPos pos) {
        this.direction = direction;
        this.transferRate = transferRate;
        this.pos = pos;
    }

    public void setNetwork(@Nullable WireNetwork network) {
        this.network = network;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (maxReceive <= 0 || this.transferRate <= 0 || this.network == null) return 0;
        long accepted = this.network.insert(this.pos, Math.min((long) this.transferRate, maxReceive), this.direction, simulate);
        return saturate(accepted);
    }

    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
    @Override public int getEnergyStored() { return 0; }
    @Override public int getMaxEnergyStored() { return this.transferRate; }
    @Override public boolean canExtract() { return false; }
    @Override public boolean canReceive() { return this.transferRate > 0; }

    private static int saturate(long value) {
        return (int) Math.min(Math.max(value, 0), Integer.MAX_VALUE);
    }
}
