/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.api.registry;

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.api.universe.celestialbody.CelestialBodyType;
import dev.galacticraft.api.universe.celestialbody.landable.teleporter.type.CelestialTeleporterType;
import dev.galacticraft.api.universe.display.CelestialDisplayType;
import dev.galacticraft.api.universe.position.CelestialPositionType;
import dev.galacticraft.mod.Constant;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.WritableRegistry;

/**
 * Forge-clean replacement for the FabricRegistryBuilder-backed static codec registries.
 *
 * <p>These retain the exact 1.20.1 {@link WritableRegistry} field ABI because Galacticraft's
 * codecs dispatch directly through these objects. Forge datapack registries are registered
 * separately by the loader bootstrap.</p>
 */
public final class BuiltInAddonRegistries {
    private BuiltInAddonRegistries() {
    }

    public static final WritableRegistry<CelestialPositionType<?>> CELESTIAL_POSITION_TYPE =
            new DefaultedMappedRegistry<>(
                    Constant.id("static").toString(),
                    AddonRegistries.CELESTIAL_POSITION_TYPE,
                    Lifecycle.experimental(),
                    false
            );

    public static final WritableRegistry<CelestialDisplayType<?>> CELESTIAL_DISPLAY_TYPE =
            new DefaultedMappedRegistry<>(
                    Constant.id("empty").toString(),
                    AddonRegistries.CELESTIAL_DISPLAY_TYPE,
                    Lifecycle.experimental(),
                    false
            );

    public static final WritableRegistry<CelestialBodyType<?>> CELESTIAL_BODY_TYPE =
            new DefaultedMappedRegistry<>(
                    Constant.id("star").toString(),
                    AddonRegistries.CELESTIAL_BODY_TYPE,
                    Lifecycle.experimental(),
                    false
            );

    public static final WritableRegistry<CelestialTeleporterType<?>> CELESTIAL_TELEPORTER_TYPE =
            new DefaultedMappedRegistry<>(
                    Constant.id("direct").toString(),
                    AddonRegistries.CELESTIAL_TELEPORTER_TYPE,
                    Lifecycle.experimental(),
                    false
            );
}
