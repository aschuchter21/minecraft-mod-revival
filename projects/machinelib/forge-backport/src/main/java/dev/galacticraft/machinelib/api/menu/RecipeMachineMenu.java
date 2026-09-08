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
package dev.galacticraft.machinelib.api.menu;

import dev.galacticraft.machinelib.api.block.entity.RecipeMachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.extensions.IForgeMenuType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Forge-compatible MachineLib 0.3 menu for recipe-processing machines. */
public class RecipeMachineMenu<C extends Container, R extends Recipe<C>,
        Machine extends RecipeMachineBlockEntity<C, R>> extends MachineMenu<Machine> {
    private int progress;
    private int maxProgress;

    public RecipeMachineMenu(int syncId, @NotNull ServerPlayer player, @NotNull Machine machine) {
        super(syncId, player, machine);
    }

    protected RecipeMachineMenu(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf,
                                int invX, int invY,
                                @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type) {
        super(syncId, inventory, buf, invX, invY, type);
        this.maxProgress = buf.readInt();
        this.progress = this.maxProgress > 0 ? buf.readInt() : 0;
    }

    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>>
    @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(
            @NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference) {
        return createType(selfReference, 84);
    }

    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>>
    @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(
            @NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference,
            int invY) {
        return createType(selfReference, 8, invY);
    }

    public static <C extends Container, R extends Recipe<C>, Machine extends RecipeMachineBlockEntity<C, R>>
    @NotNull MenuType<RecipeMachineMenu<C, R, Machine>> createType(
            @NotNull Supplier<MachineType<Machine, ? extends RecipeMachineMenu<C, R, Machine>>> selfReference,
            int invX, int invY) {
        return IForgeMenuType.create((syncId, inventory, buf) ->
                new RecipeMachineMenu<>(syncId, inventory, buf, invX, invY, selfReference.get()));
    }

    @Override
    public void registerSyncHandlers(@NotNull Consumer<MenuSyncHandler> consumer) {
        super.registerSyncHandlers(consumer);
        consumer.accept(MenuSyncHandler.simple(this.machine::getProgress, this::setProgress));
        consumer.accept(MenuSyncHandler.simple(() -> {
            R recipe = this.machine.getActiveRecipe();
            return recipe != null ? this.machine.getProcessingTime(recipe) : 0;
        }, this::setMaxProgress));
    }

    public int getProgress() { return this.progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getMaxProgress() { return this.maxProgress; }
    public void setMaxProgress(int maxProgress) { this.maxProgress = maxProgress; }
}
