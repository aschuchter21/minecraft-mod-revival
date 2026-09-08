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

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Forge 1.20.1 compatibility implementation of MachineLib's base machine menu.
 *
 * <p>The first goal is binary/source compatibility with Galacticraft 1.20.1's
 * machine menus and Forge's extended-menu factory. Slot/tank rendering and the
 * packet transport for registered sync handlers are restored in later slices.</p>
 */
public class MachineMenu<Machine extends MachineBlockEntity> extends AbstractContainerMenu {
    public final @NotNull MachineType<?, ?> type;
    public final @NotNull Machine machine;
    public final boolean server;
    public final @NotNull ContainerLevelAccess levelAccess;
    public final @Nullable ServerPlayer player;
    public final @NotNull Inventory playerInventory;
    public final @NotNull UUID playerUUID;

    private final List<MenuSyncHandler> syncHandlers = new ArrayList<>();

    /** Server-side constructor used by Galacticraft machine block entities. */
    public MachineMenu(int syncId, @NotNull ServerPlayer player, @NotNull Machine machine) {
        super(machine.getMachineType().getMenuType(), syncId);
        this.type = machine.getMachineType();
        this.machine = machine;
        this.server = true;
        this.player = player;
        this.playerInventory = player.getInventory();
        this.playerUUID = player.getUUID();
        this.levelAccess = ContainerLevelAccess.create(
                Objects.requireNonNull(machine.getLevel()), machine.getBlockPos());
        this.registerSyncHandlers(this::addSyncHandler);
    }

    /** Client-side extended-menu constructor retained from MachineLib 0.3. */
    @SuppressWarnings("unchecked")
    protected MachineMenu(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf,
                          int invX, int invY,
                          @NotNull MachineType<Machine, ? extends MachineMenu<Machine>> type) {
        super(type.getMenuType(), syncId);
        this.type = type;
        this.server = false;
        this.player = null;
        this.playerInventory = inventory;
        this.playerUUID = inventory.player.getUUID();

        BlockPos blockPos = buf.readBlockPos();
        Object blockEntity = inventory.player.level().getBlockEntity(blockPos);
        if (!(blockEntity instanceof MachineBlockEntity)) {
            throw new IllegalStateException("Missing MachineLib block entity at " + blockPos);
        }
        this.machine = (Machine) blockEntity;
        this.levelAccess = ContainerLevelAccess.create(inventory.player.level(), blockPos);
        this.registerSyncHandlers(this::addSyncHandler);
    }

    /** Create a Forge extended menu type whose constructor also receives MachineType. */
    public static <Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> @NotNull MenuType<Menu> createType(
            @NotNull MachineMenuFactory<Machine, Menu> factory,
            @NotNull Supplier<MachineType<Machine, Menu>> typeSupplier) {
        return IForgeMenuType.create((syncId, inventory, buf) ->
                factory.create(syncId, inventory, buf, typeSupplier.get()));
    }

    /** Create a Forge extended menu type from a three-argument menu constructor. */
    public static <Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> @NotNull MenuType<Menu> createType(
            @NotNull BasicMachineMenuFactory<Machine, Menu> factory) {
        return IForgeMenuType.create(factory::create);
    }

    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(
            int invX, int invY,
            @NotNull Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return IForgeMenuType.create((syncId, inventory, buf) ->
                new MachineMenu<>(syncId, inventory, buf, invX, invY, typeSupplier.get()));
    }

    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(
            int invY, @NotNull Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return createSimple(8, invY, typeSupplier);
    }

    public static <Machine extends MachineBlockEntity> @NotNull MenuType<MachineMenu<Machine>> createSimple(
            @NotNull Supplier<MachineType<Machine, MachineMenu<Machine>>> typeSupplier) {
        return createSimple(84, typeSupplier);
    }

    /** Hook used by Galacticraft subclasses to register machine-specific values. */
    public void registerSyncHandlers(@NotNull Consumer<MenuSyncHandler> consumer) {
        // Core storage/config synchronization is intentionally added in the networking slice.
    }

    public @NotNull List<MenuSyncHandler> getSyncHandlers() {
        return List.copyOf(this.syncHandlers);
    }

    private void addSyncHandler(@Nullable MenuSyncHandler handler) {
        if (handler != null) this.syncHandlers.add(handler);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.machine.isRemoved() || this.machine.getLevel() != player.level()) return false;
        double dx = player.getX() - (this.machine.getBlockPos().getX() + 0.5D);
        double dy = player.getY() - (this.machine.getBlockPos().getY() + 0.5D);
        double dz = player.getZ() - (this.machine.getBlockPos().getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz <= 64.0D;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotId) {
        // Machine slot construction/shift-click behavior is a later UI slice.
        return ItemStack.EMPTY;
    }

    @FunctionalInterface
    public interface MachineMenuFactory<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> {
        Menu create(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf,
                    @NotNull MachineType<Machine, Menu> type);
    }

    @FunctionalInterface
    public interface BasicMachineMenuFactory<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> {
        Menu create(int syncId, @NotNull Inventory inventory, @NotNull FriendlyByteBuf buf);
    }
}
