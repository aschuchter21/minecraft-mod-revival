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

package dev.galacticraft.mod.attribute.fluid;

import dev.galacticraft.mod.api.pipe.PipeNetwork;
import dev.galacticraft.mod.util.FluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge fluid capability endpoint for one side of a Galacticraft pipe. */
public class PipeFluidInsertable implements IFluidHandler {
    private final Direction direction;
    private final long maxTransfer;
    private final BlockPos pipe;
    private @Nullable PipeNetwork network;

    public PipeFluidInsertable(Direction direction, long maxTransfer, BlockPos pipe) {
        this.direction = direction;
        this.maxTransfer = maxTransfer;
        this.pipe = pipe;
    }

    public void setNetwork(@Nullable PipeNetwork network) {
        this.network = network;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank != 0) return 0;
        return (int) Math.min(Integer.MAX_VALUE, this.maxTransfer / FluidUtil.DROPLETS_PER_MILLIBUCKET);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0 && !stack.isEmpty();
    }

    @Override
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
        if (this.network == null || resource.isEmpty() || resource.getAmount() <= 0) return 0;
        int limitMb = this.getTankCapacity(0);
        int requestedMb = Math.min(resource.getAmount(), limitMb);
        if (requestedMb <= 0) return 0;

        long requestedInternal = requestedMb * FluidUtil.DROPLETS_PER_MILLIBUCKET;
        PipeNetwork.FluidStack stack = PipeNetwork.FluidStack.create(
                resource.getFluid(), resource.getTag(), requestedInternal);
        long acceptedInternal = this.network.insert(this.pipe, stack, this.direction, action.simulate());
        return (int) Math.min(requestedMb, acceptedInternal / FluidUtil.DROPLETS_PER_MILLIBUCKET);
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public String toString() {
        return "PipeFluidInsertable{" +
                "direction=" + direction +
                ", maxTransfer=" + maxTransfer +
                ", pipe=" + pipe +
                ", network=" + network +
                '}';
    }
}
