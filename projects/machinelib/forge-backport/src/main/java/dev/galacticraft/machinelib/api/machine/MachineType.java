/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

/** Source/binary-compatible server subset of MachineLib 0.3 MachineType. */
public interface MachineType<Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu> {
    static <Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu> MachineType<Machine, Menu> create(
            Block block, BlockEntityType<Machine> blockEntityType, MenuType<Menu> menuType,
            List<MachineStatus> statusDomain, Supplier<MachineEnergyStorage> energySupplier,
            Supplier<MachineItemStorage> itemSupplier) {
        return create(block, blockEntityType, menuType, statusDomain, energySupplier, itemSupplier, MachineFluidStorage::empty);
    }

    static <Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu> MachineType<Machine, Menu> create(
            Block block, BlockEntityType<Machine> blockEntityType, MenuType<Menu> menuType,
            List<MachineStatus> statusDomain, Supplier<MachineEnergyStorage> energySupplier,
            Supplier<MachineItemStorage> itemSupplier, Supplier<MachineFluidStorage> fluidSupplier) {
        if (statusDomain == null || statusDomain.isEmpty()) throw new IllegalArgumentException("Status domain cannot be empty");
        return new MachineTypeImpl<>(block, blockEntityType, menuType, statusDomain, energySupplier, itemSupplier, fluidSupplier);
    }

    List<MachineStatus> statusDomain();
    MachineEnergyStorage createEnergyStorage();
    MachineItemStorage createItemStorage();
    MachineFluidStorage createFluidStorage();
    Block getBlock();
    MenuType<Menu> getMenuType();
    BlockEntityType<Machine> getBlockEntityType();
}
