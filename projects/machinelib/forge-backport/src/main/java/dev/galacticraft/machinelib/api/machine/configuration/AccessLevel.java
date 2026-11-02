/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine.configuration;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AccessLevel implements StringRepresentable {
    PUBLIC(Component.translatable("ui.machinelib.machine.security.access.public")),
    TEAM(Component.translatable("ui.machinelib.machine.security.access.team")),
    PRIVATE(Component.translatable("ui.machinelib.machine.security.access.private"));

    public static final AccessLevel[] VALUES = values();
    private final Component name;

    AccessLevel(Component name) { this.name = name; }

    public static @NotNull AccessLevel fromString(@NotNull String value) {
        return switch (value) {
            case "public" -> PUBLIC;
            case "team" -> TEAM;
            case "private" -> PRIVATE;
            default -> throw new IllegalArgumentException("Invalid access level: " + value);
        };
    }

    public static @NotNull AccessLevel getByOrdinal(byte ordinal) {
        return VALUES[ordinal];
    }

    public @NotNull Component getName() { return this.name; }

    @Override
    public @NotNull String getSerializedName() {
        return switch (this) {
            case PUBLIC -> "public";
            case TEAM -> "team";
            case PRIVATE -> "private";
        };
    }
}
