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

package dev.galacticraft.mod.api.pipe;

import dev.galacticraft.mod.api.pipe.impl.PipeNetworkImpl;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/** Forge 1.20.1 pipe-network contract, retaining Galacticraft's internal droplet units. */
public interface PipeNetwork {
    static PipeNetwork create(ServerLevel world, long maxTransferRate) {
        return new PipeNetworkImpl(world, maxTransferRate);
    }

    boolean addPipe(@NotNull BlockPos pos, @Nullable Pipe pipe);
    void removePipe(Pipe pipe, @NotNull BlockPos removedPos);
    boolean updateConnection(@NotNull BlockPos adjacentToUpdated, @NotNull BlockPos updatedPos);

    /** @return amount accepted, in Galacticraft internal droplet units. */
    long insert(@NotNull BlockPos fromPipe, @NotNull FluidStack stack, @NotNull Direction direction, boolean simulate);
    long insertInternal(@NotNull FluidStack amount, double ratio, long available, boolean simulate);
    void getNonFullInsertables(Object2LongMap<PipeNetwork> fluidRequirement, BlockPos source,
                               @NotNull FluidStack amount);

    long getMaxTransferRate();
    Collection<BlockPos> getAllPipes();
    Map<BlockPos, IFluidHandler> getInsertable();
    boolean markedForRemoval();
    void markForRemoval();
    boolean isCompatibleWith(Pipe pipe);

    final class FluidStack {
        public static final FluidStack EMPTY = new FluidStack(null, null, 0);

        private final Fluid fluid;
        private final CompoundTag tag;
        private long amount;

        private FluidStack(@Nullable Fluid fluid, @Nullable CompoundTag tag, long amount) {
            this.fluid = fluid;
            this.tag = tag == null ? null : tag.copy();
            this.amount = amount;
        }

        public static FluidStack create(@Nullable Fluid fluid, @Nullable CompoundTag tag, long amount) {
            if (fluid == null || fluid == Fluids.EMPTY || amount <= 0) return EMPTY;
            return new FluidStack(fluid, tag, amount);
        }

        public static FluidStack create(@Nullable Fluid fluid, long amount) {
            return create(fluid, null, amount);
        }

        public boolean isEmpty() {
            return this.amount <= 0 || this.fluid == null || this.fluid == Fluids.EMPTY;
        }

        public @Nullable Fluid fluid() {
            return this.fluid;
        }

        public @Nullable CompoundTag tag() {
            return this.tag == null ? null : this.tag.copy();
        }

        public long amount() {
            return this.amount;
        }

        public void setAmount(long amount) {
            if (this == EMPTY) throw new UnsupportedOperationException("Cannot mutate EMPTY fluid stack");
            this.amount = Math.max(0, amount);
        }

        public boolean sameVariant(@NotNull FluidStack other) {
            return this.fluid == other.fluid && Objects.equals(this.tag, other.tag);
        }

        public @NotNull net.minecraftforge.fluids.FluidStack toForgeStack(int amountMb) {
            if (this.isEmpty() || amountMb <= 0) return net.minecraftforge.fluids.FluidStack.EMPTY;
            net.minecraftforge.fluids.FluidStack stack = new net.minecraftforge.fluids.FluidStack(this.fluid, amountMb);
            if (this.tag != null) stack.setTag(this.tag.copy());
            return stack;
        }

        @Override
        public String toString() {
            return "FluidStack[fluid=" + fluid + ", tag=" + tag + ", amount=" + amount + ']';
        }
    }
}
