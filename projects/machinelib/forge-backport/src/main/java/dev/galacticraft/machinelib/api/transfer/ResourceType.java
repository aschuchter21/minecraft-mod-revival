/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.transfer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

public enum ResourceType {
    NONE(Component.translatable("ui.machinelib.resource.none").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))),
    ENERGY(Component.translatable("ui.machinelib.resource.energy").setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE))),
    ITEM(Component.translatable("ui.machinelib.resource.item").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))),
    FLUID(Component.translatable("ui.machinelib.resource.fluid").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))),
    ANY(Component.translatable("ui.machinelib.resource.any").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));

    private final Component name;

    ResourceType(Component name) { this.name = name; }

    public static ResourceType getFromOrdinal(byte id) {
        return switch (id) {
            case 0 -> NONE;
            case 1 -> ENERGY;
            case 2 -> ITEM;
            case 3 -> FLUID;
            case 4 -> ANY;
            default -> throw new IllegalStateException("Unexpected id: " + id);
        };
    }

    public @NotNull Component getName() { return this.name; }
    public boolean matchesSlots() { return this != ANY && this != NONE && this != ENERGY; }
    public boolean matchesGroups() { return this != NONE && this != ENERGY; }
    public boolean willAcceptResource(ResourceType other) { return this != NONE && (this == other || this == ANY); }
}
