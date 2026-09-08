/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.compat;

import dev.galacticraft.api.rocket.RocketData;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side bridge for the rocket data carried between reaching orbit and
 * choosing a destination. The Fabric build stored this on ServerPlayer via a
 * mixin; Forge networking will consume this bridge until the full player-state
 * capability is transplanted.
 */
public final class ForgeRocketTravelState {
    private static final Map<UUID, RocketData> ROCKETS = new ConcurrentHashMap<>();

    private ForgeRocketTravelState() {
    }

    public static void set(ServerPlayer player, RocketData data) {
        ROCKETS.put(player.getUUID(), data);
    }

    public static @Nullable RocketData get(ServerPlayer player) {
        return ROCKETS.get(player.getUUID());
    }

    public static @Nullable RocketData take(ServerPlayer player) {
        return ROCKETS.remove(player.getUUID());
    }

    public static void clear(ServerPlayer player) {
        ROCKETS.remove(player.getUUID());
    }
}
