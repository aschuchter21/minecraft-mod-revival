/*
 * Copyright (c) 2021-2023 Team Galacticraft
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
package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.RecipeMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge-compatible MachineLib 0.3 base for recipe-processing machines. */
public abstract class RecipeMachineBlockEntity<C extends Container, R extends Recipe<C>> extends MachineBlockEntity {
    private final @NotNull RecipeType<R> recipeType;
    private long inventoryModCount = -1;
    private @Nullable MachineStatus cachedRecipeState;
    private @Nullable R activeRecipe;
    private @Nullable R cachedRecipe;
    private int progress;

    protected RecipeMachineBlockEntity(
            @NotNull MachineType<? extends RecipeMachineBlockEntity<C, R>,
                    ? extends RecipeMachineMenu<C, R, ? extends RecipeMachineBlockEntity<C, R>>> type,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @NotNull RecipeType<R> recipeType) {
        super(type, pos, state);
        this.recipeType = recipeType;
    }

    protected abstract @NotNull C craftingInv();
    protected abstract void outputStacks(@NotNull R recipe);
    protected abstract boolean canOutputStacks(@NotNull R recipe);
    protected abstract void extractCraftingMaterials(@NotNull R recipe);
    protected abstract @NotNull MachineStatus workingStatus(R recipe);
    protected abstract @Nullable MachineStatus hasResourcesToWork();
    protected abstract void extractResourcesToWork();

    @Override
    public @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                       @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        profiler.push("resources");
        MachineStatus status = this.hasResourcesToWork();
        profiler.pop();
        if (status != null) return status;

        profiler.push("recipe");
        MachineStatus recipeFailure = this.testInventoryRecipe(level, profiler);
        profiler.pop();
        if (recipeFailure != null) return recipeFailure;

        R recipe = this.getActiveRecipe();
        if (recipe == null) return MachineStatuses.INVALID_RECIPE;

        profiler.push("working");
        this.extractResourcesToWork();
        if (++this.progress > this.getProcessingTime(recipe)) {
            profiler.push("crafting");
            this.craft(profiler, recipe);
            profiler.pop();
        }
        profiler.pop();
        return this.workingStatus(recipe);
    }

    @Nullable
    protected MachineStatus testInventoryRecipe(@NotNull ServerLevel world, @NotNull ProfilerFiller profiler) {
        if (this.inventoryModCount != this.itemStorage().getModifications()) {
            this.inventoryModCount = this.itemStorage().getModifications();
            profiler.push("find_recipe");
            R recipe = this.findValidRecipe(world);
            profiler.pop();
            if (recipe != null) {
                if (this.canOutputStacks(recipe)) {
                    this.setActiveRecipe(recipe);
                    this.cachedRecipeState = null;
                } else {
                    this.setActiveRecipe(null);
                    this.cachedRecipeState = MachineStatuses.OUTPUT_FULL;
                }
            } else {
                this.setActiveRecipe(null);
                this.cachedRecipeState = MachineStatuses.INVALID_RECIPE;
            }
        }
        return this.cachedRecipeState;
    }

    protected void craft(@NotNull ProfilerFiller profiler, @NotNull R recipe) {
        profiler.push("extract_materials");
        this.extractCraftingMaterials(recipe);
        profiler.popPush("output_stacks");
        this.outputStacks(recipe);
        profiler.pop();
        this.setActiveRecipe(null);
    }

    public @NotNull RecipeType<R> getRecipeType() { return this.recipeType; }

    protected @Nullable R findValidRecipe(@NotNull Level world) {
        if (this.cachedRecipe != null && this.cachedRecipe.matches(this.craftingInv(), world)) {
            return this.cachedRecipe;
        }
        return world.getRecipeManager().getRecipeFor(this.getRecipeType(), this.craftingInv(), world).orElse(null);
    }

    @Override
    public void writeScreenOpeningData(@NotNull ServerPlayer player, @NotNull FriendlyByteBuf buf) {
        super.writeScreenOpeningData(player, buf);
        if (this.activeRecipe != null) {
            buf.writeInt(this.getProcessingTime(this.activeRecipe));
            buf.writeInt(this.progress);
        } else {
            buf.writeInt(0);
        }
    }

    public abstract int getProcessingTime(@NotNull R recipe);
    public int getProgress() { return this.progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public @Nullable R getActiveRecipe() { return this.activeRecipe; }

    protected void setActiveRecipe(@Nullable R recipe) {
        if (recipe != null) this.cachedRecipe = recipe;
        if (this.activeRecipe != recipe) {
            this.activeRecipe = recipe;
            this.progress = 0;
        } else if (recipe == null) {
            this.progress = 0;
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("Progress", this.progress);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.progress = nbt.getInt("Progress");
    }
}
