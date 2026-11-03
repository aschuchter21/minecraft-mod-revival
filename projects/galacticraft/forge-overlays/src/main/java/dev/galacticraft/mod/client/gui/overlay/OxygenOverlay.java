/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.client.gui.overlay;

import dev.galacticraft.api.accessor.GearInventoryProvider;
import dev.galacticraft.api.gas.Gases;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.util.DrawableUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/**
 * Moon/space oxygen HUD using Forge item-fluid capabilities and the synchronized
 * original Galacticraft 12-slot player gear inventory.
 */
public final class OxygenOverlay {
    private OxygenOverlay() {
    }

    public static void onHudRender(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        CelestialBody<?, ?> body = CelestialBody.getByDimension(minecraft.level).orElse(null);
        if (body == null || body.atmosphere().breathable()) return;
        if (!(minecraft.player instanceof GearInventoryProvider gear)) return;

        int w = minecraft.getWindow().getGuiScaledWidth();
        graphics.fill(w - Constant.TextureCoordinate.OVERLAY_WIDTH - 11, 4,
                w - Constant.TextureCoordinate.OVERLAY_WIDTH - 9,
                6 + Constant.TextureCoordinate.OVERLAY_HEIGHT, 0xAA000000);
        graphics.fill(w - Constant.TextureCoordinate.OVERLAY_WIDTH - 6, 4,
                w - 4, 6 + Constant.TextureCoordinate.OVERLAY_HEIGHT, 0xAA000000);

        Container tanks = gear.getOxygenTanks();
        for (int i = 0; i < tanks.getContainerSize(); i++) {
            ItemStack stack = tanks.getItem(i);
            int x = w - Constant.TextureCoordinate.OVERLAY_WIDTH * i - 5 * (i + 4);
            if (!stack.isEmpty()) {
                stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler -> {
                    if (handler.getTanks() > 0) {
                        int capacity = Math.max(1, handler.getTankCapacity(0));
                        int amount = handler.getFluidInTank(0).getFluid() == Gases.OXYGEN
                                ? handler.getFluidInTank(0).getAmount() : 0;
                        DrawableUtil.drawOxygenBuffer(graphics.pose(), x, 5, amount, capacity);
                    }
                });
            } else if (minecraft.player.isCreative()) {
                DrawableUtil.drawOxygenBuffer(graphics.pose(), x, 5, 1, 1);
            }
        }
    }
}
