/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.compat.vanilla.RecipeTestContainer;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.RecipeMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/** Forge-compatible MachineLib 0.3 base for simple item-in/item-out recipe machines. */
public abstract class BasicRecipeMachineBlockEntity<C extends Container, R extends Recipe<C>>
        extends RecipeMachineBlockEntity<C, R> {
    protected final @NotNull Container craftingInv;
    protected final int inputSlots;
    protected final int inputSlotsLen;
    protected final int outputSlots;
    protected final int outputSlotsLen;

    protected BasicRecipeMachineBlockEntity(
            @NotNull MachineType<? extends BasicRecipeMachineBlockEntity<C, R>,
                    ? extends RecipeMachineMenu<C, R, ? extends BasicRecipeMachineBlockEntity<C, R>>> type,
            @NotNull BlockPos pos, BlockState state, @NotNull RecipeType<R> recipeType,
            int inputSlot, int outputSlot) {
        this(type, pos, state, recipeType, inputSlot, 1, outputSlot);
    }

    protected BasicRecipeMachineBlockEntity(
            @NotNull MachineType<? extends BasicRecipeMachineBlockEntity<C, R>,
                    ? extends RecipeMachineMenu<C, R, ? extends BasicRecipeMachineBlockEntity<C, R>>> type,
            @NotNull BlockPos pos, BlockState state, @NotNull RecipeType<R> recipeType,
            int inputSlots, int inputSlotsLen, int outputSlot) {
        this(type, pos, state, recipeType, inputSlots, inputSlotsLen, outputSlot, 1);
    }

    protected BasicRecipeMachineBlockEntity(
            @NotNull MachineType<? extends BasicRecipeMachineBlockEntity<C, R>,
                    ? extends RecipeMachineMenu<C, R, ? extends BasicRecipeMachineBlockEntity<C, R>>> type,
            @NotNull BlockPos pos, BlockState state, @NotNull RecipeType<R> recipeType,
            int inputSlots, int inputSlotsLen, int outputSlots, int outputSlotsLen) {
        super(type, pos, state, recipeType);
        this.inputSlots = inputSlots;
        this.inputSlotsLen = inputSlotsLen;
        this.outputSlots = outputSlots;
        this.outputSlotsLen = outputSlotsLen;
        this.craftingInv = RecipeTestContainer.create(this.itemStorage(), this.inputSlots, this.inputSlotsLen);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @NotNull C craftingInv() {
        return (C) this.craftingInv;
    }

    @Override
    protected void outputStacks(@NotNull R recipe) {
        ItemStack assembled = recipe.assemble(this.craftingInv(), this.level.registryAccess());
        this.itemStorage().insertMatching(this.outputSlots, this.outputSlotsLen,
                assembled.getItem(), assembled.getTag(), assembled.getCount());
    }

    @Override
    protected boolean canOutputStacks(@NotNull R recipe) {
        ItemStack assembled = recipe.assemble(this.craftingInv(), this.level.registryAccess());
        return this.itemStorage().canInsert(this.outputSlots, this.outputSlotsLen,
                assembled.getItem(), assembled.getTag(), assembled.getCount());
    }

    @Override
    protected void extractCraftingMaterials(@NotNull R recipe) {
        for (int i = 0; i < this.inputSlotsLen; i++) {
            this.itemStorage().consumeOne(this.inputSlots + i);
        }
    }
}
