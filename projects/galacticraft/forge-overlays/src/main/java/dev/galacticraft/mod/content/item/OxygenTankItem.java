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

package dev.galacticraft.mod.content.item;

import dev.galacticraft.api.accessor.GearInventoryProvider;
import dev.galacticraft.api.gas.Gases;
import dev.galacticraft.mod.Constant;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Forge 1.20.1 oxygen tank preserving Galacticraft's original internal oxygen units. */
public class OxygenTankItem extends Item {
    /** Fabric Transfer API uses 81 droplets per Forge millibucket. */
    public static final long INTERNAL_UNITS_PER_MB = 81L;

    public final int capacity;

    public OxygenTankItem(Properties settings, int capacity) {
        super(settings.durability(capacity));
        this.capacity = capacity;
    }

    public long getStoredOxygen(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        return Math.min(this.capacity, Math.max(0, tag.getLong(Constant.Nbt.VALUE)));
    }

    public void setStoredOxygen(ItemStack stack, long amount) {
        long clamped = Math.min(this.capacity, Math.max(0, amount));
        CompoundTag tag = stack.getOrCreateTag();
        if (clamped == 0) tag.remove(Constant.Nbt.VALUE);
        else tag.putLong(Constant.Nbt.VALUE, clamped);
    }

    private int getCapacityMb() {
        return (int) Math.min(Integer.MAX_VALUE, this.capacity / INTERNAL_UNITS_PER_MB);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IFluidHandlerItem> fluid = LazyOptional.of(() -> new OxygenFluidHandler(stack));

            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return cap == ForgeCapabilities.FLUID_HANDLER_ITEM ? this.fluid.cast() : LazyOptional.empty();
            }
        };
    }

    private final class OxygenFluidHandler implements IFluidHandlerItem {
        private final ItemStack stack;

        private OxygenFluidHandler(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public @NotNull ItemStack getContainer() {
            return this.stack;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (tank != 0) return FluidStack.EMPTY;
            int amountMb = (int) Math.min(Integer.MAX_VALUE, getStoredOxygen(this.stack) / INTERNAL_UNITS_PER_MB);
            return amountMb <= 0 ? FluidStack.EMPTY : new FluidStack(Gases.OXYGEN, amountMb);
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? getCapacityMb() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack resource) {
            return tank == 0 && !resource.isEmpty() && resource.getFluid() == Gases.OXYGEN;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!this.isFluidValid(0, resource) || resource.getAmount() <= 0) return 0;
            long stored = getStoredOxygen(this.stack);
            long remainingUnits = Math.max(0, capacity - stored);
            int acceptedMb = (int) Math.min(resource.getAmount(), remainingUnits / INTERNAL_UNITS_PER_MB);
            if (acceptedMb > 0 && action.execute()) {
                setStoredOxygen(this.stack, stored + acceptedMb * INTERNAL_UNITS_PER_MB);
            }
            return acceptedMb;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || resource.getFluid() != Gases.OXYGEN || resource.getAmount() <= 0) {
                return FluidStack.EMPTY;
            }
            return this.drain(resource.getAmount(), action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) return FluidStack.EMPTY;
            long stored = getStoredOxygen(this.stack);
            int availableMb = (int) Math.min(Integer.MAX_VALUE, stored / INTERNAL_UNITS_PER_MB);
            int drainedMb = Math.min(maxDrain, availableMb);
            if (drainedMb <= 0) return FluidStack.EMPTY;
            if (action.execute()) {
                setStoredOxygen(this.stack, stored - drainedMb * INTERNAL_UNITS_PER_MB);
            }
            return new FluidStack(Gases.OXYGEN, drainedMb);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * (float) this.getStoredOxygen(stack) / (float) this.capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float scale = 1.0F - ((float) this.getStoredOxygen(stack) / (float) this.capacity);
        return Constant.Text.Color.getStorageLevelColor(scale);
    }

    @Override
    public int getEnchantmentValue() {
        return -1;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return this.capacity <= 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> lines, TooltipFlag context) {
        long amount = this.getStoredOxygen(stack);
        lines.add(Component.translatable("tooltip.galacticraft.oxygen_remaining", amount + "/" + this.capacity)
                .setStyle(Constant.Text.Color.getStorageLevelStyle(1.0 - ((double) amount / (double) this.capacity))));
        super.appendHoverText(stack, world, lines, context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack held = user.getItemInHand(hand);
        if (user instanceof GearInventoryProvider gearProvider) {
            Container tanks = gearProvider.getOxygenTanks();
            for (int slot = 0; slot < tanks.getContainerSize(); slot++) {
                if (tanks.getItem(slot).isEmpty()) {
                    if (!world.isClientSide()) {
                        ItemStack inserted = held.copy();
                        inserted.setCount(1);
                        tanks.setItem(slot, inserted);
                        held.shrink(1);
                        user.setItemInHand(hand, held);
                    }
                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, user.getItemInHand(hand));
                }
            }
        }
        return super.use(world, user, hand);
    }
}
