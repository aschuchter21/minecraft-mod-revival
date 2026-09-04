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

import dev.galacticraft.api.gas.Gases;
import dev.galacticraft.mod.Constant;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Forge 1.20.1 creative oxygen tank. */
public class InfiniteOxygenTankItem extends Item {
    private int ticks = (int) (Math.random() * 1000.0);

    public InfiniteOxygenTankItem(Properties settings) {
        super(settings.stacksTo(1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IFluidHandlerItem> fluid = LazyOptional.of(() -> new InfiniteOxygenFluidHandler(stack));

            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return cap == ForgeCapabilities.FLUID_HANDLER_ITEM ? this.fluid.cast() : LazyOptional.empty();
            }
        };
    }

    private static final class InfiniteOxygenFluidHandler implements IFluidHandlerItem {
        private final ItemStack stack;

        private InfiniteOxygenFluidHandler(ItemStack stack) {
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
            return tank == 0 ? new FluidStack(Gases.OXYGEN, Integer.MAX_VALUE) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? Integer.MAX_VALUE : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack resource) {
            return tank == 0 && !resource.isEmpty() && resource.getFluid() == Gases.OXYGEN;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || resource.getFluid() != Gases.OXYGEN || resource.getAmount() <= 0) {
                return FluidStack.EMPTY;
            }
            return new FluidStack(Gases.OXYGEN, resource.getAmount());
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return maxDrain <= 0 ? FluidStack.EMPTY : new FluidStack(Gases.OXYGEN, maxDrain);
        }
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
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        tooltip.add(Component.translatable("tooltip.galacticraft.oxygen_remaining",
                Component.translatable("tooltip.galacticraft.infinite").setStyle(Constant.Text.Color.getRainbow(this.ticks))));
        tooltip.add(Component.translatable("tooltip.galacticraft.creative_only").setStyle(Constant.Text.Color.LIGHT_PURPLE_STYLE));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 13;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (++this.ticks > 1000) this.ticks = 0;
        return Mth.hsvToRgb(this.ticks / 1000.0f, 1, 1);
    }
}
