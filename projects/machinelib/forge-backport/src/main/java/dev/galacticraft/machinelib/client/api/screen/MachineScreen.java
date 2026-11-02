/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.client.api.screen;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Forge-native MachineLib 0.3 machine screen base.
 *
 * <p>This restores the client ABI and the shared rendering shell used by
 * Galacticraft's machine screens. Fabric transfer/configuration interactions are
 * ported separately through Forge networking rather than emulated here.</p>
 */
public class MachineScreen<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>>
        extends AbstractContainerScreen<Menu> {
    private static final NumberFormat NUMBER = NumberFormat.getIntegerInstance();

    private final @NotNull ResourceLocation texture;

    protected int capacitorX = 8;
    protected int capacitorY = 8;
    protected int capacitorHeight = 48;

    protected MachineScreen(@NotNull Menu handler, @NotNull Component title, @NotNull ResourceLocation texture) {
        super(handler, handler.playerInventory, title);
        this.texture = texture;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    public void appendEnergyTooltip(List<Component> list) {
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.renderForeground(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected final void renderBg(@NotNull GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        this.renderBackground(graphics);
        graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.renderBackground(graphics, mouseX, mouseY, delta);
        this.drawTanks(graphics, mouseX, mouseY);
        this.drawCapacitor(graphics, mouseX, mouseY);
    }

    /**
     * Extension hook used by Galacticraft's individual machine screens.
     */
    protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }

    /**
     * Tank rendering is restored in the Forge fluid-client slice. Keeping the
     * hook here preserves the original subclass ABI (for example FuelLoader).
     */
    protected void drawTanks(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    protected void drawCapacitor(GuiGraphics graphics, int mouseX, int mouseY) {
        long capacity = this.menu.energyStorage.getCapacity();
        if (capacity <= 0 || this.capacitorHeight <= 0) return;

        long amount = Math.max(0L, Math.min(this.menu.energyStorage.getAmount(), capacity));
        int filled = (int) Math.floor((double) amount / (double) capacity * this.capacitorHeight);

        int x1 = this.leftPos + this.capacitorX;
        int y1 = this.topPos + this.capacitorY;
        int x2 = x1 + 8;
        int y2 = y1 + this.capacitorHeight;

        graphics.fill(x1, y1, x2, y2, 0xFF202020);
        if (filled > 0) {
            graphics.fill(x1 + 1, y2 - filled, x2 - 1, y2 - 1, 0xFF6C4FE0);
        }

        if (mouseIn(mouseX, mouseY, x1, y1, x2 - x1, y2 - y1)) {
            List<Component> lines = new ArrayList<>();
            MachineStatus status = this.menu.state.getStatus();
            if (status != null) {
                lines.add(Component.translatable("ui.machinelib.machine.status")
                        .withStyle(ChatFormatting.GRAY)
                        .append(status.getText()));
            }
            lines.add(Component.literal(NUMBER.format(amount) + " / " + NUMBER.format(capacity) + " gJ")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));
            this.appendEnergyTooltip(lines);
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }

    protected void drawTitle(@NotNull GuiGraphics graphics) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFFFF, false);
    }

    protected void drawSlotOverlay(@NotNull GuiGraphics graphics, int x, int y, int color) {
        color = (color & 0x00FFFFFF) | 0x70000000;
        graphics.fill(this.leftPos + x, this.topPos + y, this.leftPos + x + 16, this.topPos + y + 16, color);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected final void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        this.drawTitle(graphics);
    }

    public int getX() {
        return this.leftPos;
    }

    public int getY() {
        return this.topPos;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    private static boolean mouseIn(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }
}
