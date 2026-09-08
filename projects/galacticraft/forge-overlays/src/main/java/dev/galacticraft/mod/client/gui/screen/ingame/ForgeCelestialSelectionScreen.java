/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.client.gui.screen.ingame;

import dev.galacticraft.api.rocket.RocketData;
import dev.galacticraft.api.satellite.Satellite;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.api.universe.celestialbody.landable.Landable;
import dev.galacticraft.mod.forge.network.ForgeRocketNetworking;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Forge 1.20.1 client bridge for the recovered Galacticraft celestial map.
 *
 * <p>All of the original map rendering, selection, tier checks and input behavior
 * remain in {@link CelestialSelectionScreen}; only the Fabric C2S destination send
 * is replaced here.</p>
 */
public final class ForgeCelestialSelectionScreen extends CelestialSelectionScreen {
    private final RocketData forgeRocketData;

    public ForgeCelestialSelectionScreen(boolean mapMode, RocketData data, boolean canCreateStations,
                                         @Nullable CelestialBody<?, ?> fromBody) {
        super(mapMode, data, canCreateStations, fromBody);
        this.forgeRocketData = data;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void teleportToSelectedBody() {
        assert !this.mapMode;
        if (this.selectedBody == null || !(this.selectedBody.type() instanceof Landable landable)) return;
        if (this.forgeRocketData != RocketData.empty()
                && !this.forgeRocketData.canTravel(this.manager, this.fromBody, this.selectedBody)) {
            return;
        }

        ResourceLocation bodyId = this.celestialBodyRegistry.getKey(this.selectedBody);
        if (bodyId == null) return;

        ForgeRocketNetworking.selectPlanet(bodyId);

        if (this.minecraft != null) {
            String destinationKey;
            if (this.selectedBody.type() instanceof Satellite satellite) {
                destinationKey = satellite.getCustomName(this.selectedBody.config()).getString();
            } else if (this.selectedBody.name().getContents() instanceof TranslatableContents translatable) {
                destinationKey = translatable.getKey();
            } else {
                destinationKey = this.selectedBody.name().getString();
            }

            this.minecraft.setScreen(new SpaceTravelScreen(
                    destinationKey,
                    landable.world(this.selectedBody.config())
            ));
        }
    }
}
