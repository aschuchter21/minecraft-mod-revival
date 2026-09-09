package dev.orbitalindustries.client.screen;

import dev.orbitalindustries.menu.MissionControlMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MissionControlScreen extends AbstractContainerScreen<MissionControlMenu> {
    public MissionControlScreen(MissionControlMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 180;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF0B1118);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + 38, 0xFF172431);
        graphics.fill(x + 10, y + 48, x + 122, y + 96, 0xFF13202A);
        graphics.fill(x + 134, y + 48, x + 246, y + 96, 0xFF13202A);
        graphics.fill(x + 10, y + 106, x + 84, y + 165, 0xFF13202A);
        graphics.fill(x + 91, y + 106, x + 165, y + 165, 0xFF13202A);
        graphics.fill(x + 172, y + 106, x + 246, y + 165, 0xFF13202A);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 12, 10, 0xFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.orbitalindustries.mission_control.subtitle"), 12, 24, 0x79C7FF, false);

        drawMetric(graphics, Component.translatable("screen.orbitalindustries.mission_control.stations"), menu.stationCount(), 18, 57);
        drawMetric(graphics, Component.translatable("screen.orbitalindustries.mission_control.colonies"), menu.colonyCount(), 142, 57);
        drawMetric(graphics, Component.translatable("screen.orbitalindustries.mission_control.satellites"), menu.satelliteCount(), 18, 116);
        drawMetric(graphics, Component.translatable("screen.orbitalindustries.mission_control.depots"), menu.depotCount(), 99, 116);
        drawMetric(graphics, Component.translatable("screen.orbitalindustries.mission_control.routes"), menu.routeCount(), 180, 116);
    }

    private void drawMetric(GuiGraphics graphics, Component label, int value, int x, int y) {
        graphics.drawString(font, label, x, y, 0xAAB7C4, false);
        graphics.drawString(font, Integer.toString(value), x, y + 16, 0xFFFFFF, false);
    }
}
