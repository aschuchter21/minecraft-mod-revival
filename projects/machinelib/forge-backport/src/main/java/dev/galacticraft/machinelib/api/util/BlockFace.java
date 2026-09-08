/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Machine-relative block face used by MachineLib I/O configuration. */
public enum BlockFace {
    FRONT(Component.translatable("ui.machinelib.face.front"), true),
    RIGHT(Component.translatable("ui.machinelib.face.right"), true),
    BACK(Component.translatable("ui.machinelib.face.back"), true),
    LEFT(Component.translatable("ui.machinelib.face.left"), true),
    TOP(Component.translatable("ui.machinelib.face.top"), false),
    BOTTOM(Component.translatable("ui.machinelib.face.bottom"), false);

    private final Component name;
    private final boolean side;

    BlockFace(Component name, boolean side) {
        this.name = name.copy().withStyle(ChatFormatting.GOLD);
        this.side = side;
    }

    public static @Nullable BlockFace toFace(@NotNull Direction facing, @Nullable Direction target) {
        if (target == null) return null;
        if (target == Direction.DOWN) return BOTTOM;
        if (target == Direction.UP) return TOP;
        return switch (facing) {
            case NORTH -> switch (target) {
                case NORTH -> FRONT;
                case EAST -> RIGHT;
                case SOUTH -> BACK;
                case WEST -> LEFT;
                default -> throw new IllegalStateException("Unexpected direction: " + target);
            };
            case EAST -> switch (target) {
                case EAST -> FRONT;
                case NORTH -> LEFT;
                case WEST -> BACK;
                case SOUTH -> RIGHT;
                default -> throw new IllegalStateException("Unexpected direction: " + target);
            };
            case SOUTH -> switch (target) {
                case SOUTH -> FRONT;
                case WEST -> RIGHT;
                case NORTH -> BACK;
                case EAST -> LEFT;
                default -> throw new IllegalStateException("Unexpected direction: " + target);
            };
            case WEST -> switch (target) {
                case WEST -> FRONT;
                case SOUTH -> LEFT;
                case EAST -> BACK;
                case NORTH -> RIGHT;
                default -> throw new IllegalStateException("Unexpected direction: " + target);
            };
            default -> throw new IllegalArgumentException("Facing must be horizontal: " + facing);
        };
    }

    public Component getName() { return this.name; }

    public @NotNull Direction toDirection(@NotNull Direction facing) {
        if (this == BOTTOM) return Direction.DOWN;
        if (this == TOP) return Direction.UP;
        return switch (facing) {
            case NORTH -> switch (this) {
                case FRONT -> Direction.NORTH;
                case RIGHT -> Direction.EAST;
                case BACK -> Direction.SOUTH;
                case LEFT -> Direction.WEST;
                default -> throw new IllegalStateException();
            };
            case EAST -> switch (this) {
                case RIGHT -> Direction.SOUTH;
                case FRONT -> Direction.EAST;
                case LEFT -> Direction.NORTH;
                case BACK -> Direction.WEST;
                default -> throw new IllegalStateException();
            };
            case SOUTH -> switch (this) {
                case BACK -> Direction.NORTH;
                case LEFT -> Direction.EAST;
                case FRONT -> Direction.SOUTH;
                case RIGHT -> Direction.WEST;
                default -> throw new IllegalStateException();
            };
            case WEST -> switch (this) {
                case LEFT -> Direction.SOUTH;
                case BACK -> Direction.EAST;
                case RIGHT -> Direction.NORTH;
                case FRONT -> Direction.WEST;
                default -> throw new IllegalStateException();
            };
            default -> throw new IllegalArgumentException("Facing must be horizontal: " + facing);
        };
    }

    public @NotNull BlockFace getOpposite() {
        return switch (this) {
            case BOTTOM -> TOP;
            case TOP -> BOTTOM;
            case BACK -> FRONT;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case FRONT -> BACK;
        };
    }

    public boolean side() { return this.side; }
    public boolean base() { return !this.side; }
}
