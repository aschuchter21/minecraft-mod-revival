/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.impl.machine;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class MachineTypeImpl<Machine extends MachineBlockEntity, Menu extends AbstractContainerMenu>
        implements MachineType<Machine, Menu> {
    private final Block block;
    private final BlockEntityType<Machine> blockEntityType;
    private final MenuType<Menu> menuType;
    private final Supplier<MachineEnergyStorage> energySupplier;
    private final Supplier<MachineItemStorage> itemSupplier;
    private final Supplier<MachineFluidStorage> fluidSupplier;

    public MachineTypeImpl(Block block, BlockEntityType<Machine> blockEntityType, MenuType<Menu> menuType,
                           Supplier<MachineEnergyStorage> energySupplier,
                           Supplier<MachineItemStorage> itemSupplier,
                           Supplier<MachineFluidStorage> fluidSupplier) {
        this.block = block;
        this.blockEntityType = blockEntityType;
        this.menuType = menuType;
        this.energySupplier = energySupplier;
        this.itemSupplier = itemSupplier;
        this.fluidSupplier = fluidSupplier;
    }

    @Override public MachineEnergyStorage createEnergyStorage() { return this.energySupplier.get(); }
    @Override public MachineItemStorage createItemStorage() { return this.itemSupplier.get(); }
    @Override public MachineFluidStorage createFluidStorage() { return this.fluidSupplier.get(); }
    @Override public Block getBlock() { return this.block; }
    @Override public MenuType<Menu> getMenuType() { return this.menuType; }
    @Override public BlockEntityType<Machine> getBlockEntityType() { return this.blockEntityType; }
}
