/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.network;

import dev.galacticraft.api.registry.AddonRegistries;
import dev.galacticraft.api.rocket.RocketData;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.mod.client.gui.screen.ingame.CelestialSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Client-only handlers for Forge rocket packets. */
@OnlyIn(Dist.CLIENT)
public final class ForgeRocketClientPackets {
    private ForgeRocketClientPackets() {
    }

    public static void openPlanetMenu(ForgeRocketNetworking.OpenPlanetMenuPacket message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) return;

        CelestialBody<?, ?> currentBody = null;
        if (message.currentBodyId() != -1) {
            currentBody = minecraft.getConnection().registryAccess()
                    .registryOrThrow(AddonRegistries.CELESTIAL_BODY)
                    .getHolder(message.currentBodyId())
                    .orElseThrow()
                    .value();
        }

        minecraft.setScreen(new CelestialSelectionScreen(
                false,
                RocketData.fromNbt(message.rocketData()),
                true,
                currentBody
        ));
    }
}
