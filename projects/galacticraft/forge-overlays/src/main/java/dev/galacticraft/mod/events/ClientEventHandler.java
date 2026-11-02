/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.events;

import dev.galacticraft.api.rocket.LaunchStage;
import dev.galacticraft.api.rocket.entity.Rocket;
import dev.galacticraft.mod.client.sounds.RocketSound;
import dev.galacticraft.mod.content.entity.RocketEntity;
import net.minecraft.client.Minecraft;

/** Forge-recompiled client hook for the loader-neutral RocketEvents bus. */
public final class ClientEventHandler {
    private ClientEventHandler() {
    }

    public static void init() {
        RocketEvents.STAGE_CHANGED.register(ClientEventHandler::rocketLaunchSound);
    }

    public static void rocketLaunchSound(Rocket rocket, LaunchStage oldStage) {
        if (rocket instanceof RocketEntity rocketEntity && rocket.getLaunchStage() == LaunchStage.IGNITED) {
            Minecraft.getInstance().getSoundManager().play(new RocketSound(rocketEntity));
        }
    }
}
