/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.forge.compat;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

/**
 * Forge compatibility base for the 1.20.1 Galacticraft machine sources.
 *
 * <p>MachineLib 0.3 exposed item-energy helpers by flat slot index. The first
 * Forge backport checkpoint moved those helpers to slot groups. Galacticraft's
 * real 1.20.1 machine classes still call the index-based API, so this class
 * restores that surface while using Forge Energy capabilities internally.</p>
 *
 * <p>This keeps loader-specific item capability handling out of Galacticraft's
 * machine logic and lets later machine overlays stay close to the MIT-licensed
 * upstream source.</p>
 */
public abstract class LegacyMachineBlockEntity extends MachineBlockEntity {
    protected LegacyMachineBlockEntity(
            @NotNull MachineType<? extends MachineBlockEntity, ? extends AbstractContainerMenu> type,
            @NotNull BlockPos pos,
            @NotNull BlockState state) {
        super(type, pos, state);
    }

    /** Move energy from an item in the given flat slot into the machine. */
    protected final void chargeFromStack(int slotIndex) {
        if (this.energyStorage().isFull()) return;
        if (slotIndex < 0 || slotIndex >= this.itemStorage().size()) return;

        ItemResourceSlot slot = this.itemStorage().getSlot(slotIndex);
        ItemStack stack = slot.toStack();
        if (stack.isEmpty()) return;

        IEnergyStorage source = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (source == null || !source.canExtract()) return;

        long room = this.energyStorage().tryInsert(this.getEnergyItemExtractionRate());
        int requested = saturatingInt(room);
        int extracted = source.extractEnergy(requested, false);
        if (extracted <= 0) return;

        this.energyStorage().insert(extracted);
        slot.setStack(stack);
    }

    /** Move energy from the machine into an item in the given flat slot. */
    protected final void drainPowerToStack(int slotIndex) {
        if (this.energyStorage().isEmpty()) return;
        if (slotIndex < 0 || slotIndex >= this.itemStorage().size()) return;

        ItemResourceSlot slot = this.itemStorage().getSlot(slotIndex);
        ItemStack stack = slot.toStack();
        if (stack.isEmpty()) return;

        IEnergyStorage target = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (target == null || !target.canReceive()) return;

        long available = this.energyStorage().tryExtract(this.getEnergyItemInsertionRate());
        int offered = saturatingInt(available);
        int accepted = target.receiveEnergy(offered, false);
        if (accepted <= 0) return;

        this.energyStorage().extract(accepted);
        slot.setStack(stack);
    }

    private static int saturatingInt(long amount) {
        if (amount <= 0) return 0;
        return amount >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}
