/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.impl.machine.MachineTypeImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;
import java.util.function.Supplier;

/** MachineLib 0.2-compatible machine descriptor used by Galacticraft 1.20.1. */
public interface MachineType<Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu> {
    static <Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu> MachineType<Machine, Menu> create(
            Block block, BlockEntityType<Machine> blockEntityType, MenuType<Menu> menuType,
            Supplier<MachineEnergyStorage> energySupplier, Supplier<MachineItemStorage> itemSupplier) {
        return new MachineTypeImpl<>(block, blockEntityType, menuType, energySupplier, itemSupplier, MachineFluidStorage::empty);
    }

    static <Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu> MachineType<Machine, Menu> create(
            Block block, BlockEntityType<Machine> blockEntityType, MenuType<Menu> menuType,
            Supplier<MachineEnergyStorage> energySupplier, Supplier<MachineItemStorage> itemSupplier,
            Supplier<MachineFluidStorage> fluidSupplier) {
        return new MachineTypeImpl<>(block, blockEntityType, menuType, energySupplier, itemSupplier, fluidSupplier);
    }

    /** Compatibility overload retained from the first Forge scaffold. */
    static <Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu> MachineType<Machine, Menu> create(
            Block block, BlockEntityType<Machine> blockEntityType, MenuType<Menu> menuType,
            List<MachineStatus> ignoredStatusDomain, Supplier<MachineEnergyStorage> energySupplier,
            Supplier<MachineItemStorage> itemSupplier) {
        return create(block, blockEntityType, menuType, energySupplier, itemSupplier);
    }

    /**
     * Galacticraft 1.20.1 also supplies a machine status domain for fluid-backed machines.
     * MachineLib 0.3 no longer stores that domain in MachineType, but keeping this overload preserves
     * the original Galacticraft call shape while delegating to the Forge storage implementation.
     */
    static <Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu> MachineType<Machine, Menu> create(
            Block block, BlockEntityType<Machine> blockEntityType, MenuType<Menu> menuType,
            List<MachineStatus> ignoredStatusDomain, Supplier<MachineEnergyStorage> energySupplier,
            Supplier<MachineItemStorage> itemSupplier, Supplier<MachineFluidStorage> fluidSupplier) {
        return create(block, blockEntityType, menuType, energySupplier, itemSupplier, fluidSupplier);
    }

    MachineEnergyStorage createEnergyStorage();
    MachineItemStorage createItemStorage();
    MachineFluidStorage createFluidStorage();
    Block getBlock();
    MenuType<Menu> getMenuType();
    BlockEntityType<Machine> getBlockEntityType();
}
