/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.forge;

import com.mojang.logging.LogUtils;
import dev.galacticraft.api.registry.AddonRegistries;
import dev.galacticraft.api.registry.BuiltInRocketRegistries;
import dev.galacticraft.api.registry.RocketRegistries;
import dev.galacticraft.api.rocket.part.RocketBody;
import dev.galacticraft.api.rocket.part.RocketBooster;
import dev.galacticraft.api.rocket.part.RocketBottom;
import dev.galacticraft.api.rocket.part.RocketCone;
import dev.galacticraft.api.rocket.part.RocketFin;
import dev.galacticraft.api.rocket.part.RocketUpgrade;
import dev.galacticraft.api.rocket.recipe.RocketPartRecipe;
import dev.galacticraft.api.rocket.travelpredicate.ConfiguredTravelPredicate;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.api.universe.celestialbody.landable.teleporter.CelestialTeleporter;
import dev.galacticraft.api.universe.galaxy.Galaxy;
import dev.galacticraft.impl.universe.BuiltinObjects;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.entity.data.GCEntityDataSerializers;
import dev.galacticraft.mod.data.gen.SatelliteChunkGenerator;
import dev.galacticraft.mod.forge.network.ForgeRocketNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

/** Forge 47.4.10 bootstrap for the recovered Galacticraft 1.20.1 runtime. */
@Mod(GalacticraftForgeBootstrap.MOD_ID)
public final class GalacticraftForgeBootstrap {
    public static final String MOD_ID = "galacticraft";
    private static final Logger LOGGER = LogUtils.getLogger();

    public GalacticraftForgeBootstrap() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // These loader-neutral registries back Galacticraft's dispatch codecs and must
        // contain their built-in type objects before Forge creates the synced datapack registries.
        BuiltinObjects.register();
        BuiltInRocketRegistries.initialize();
        modBus.addListener(this::registerDataPackRegistries);
        modBus.addListener(this::registerForgeAndVanillaEntries);

        ForgeRocketNetworking.register();
        LOGGER.info("Galacticraft Forge 1.20.1 runtime bootstrap loaded");
    }

    /** Forge replacement for Fabric's DynamicRegistries.registerSynced calls. */
    private void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(AddonRegistries.CELESTIAL_BODY,
                CelestialBody.DIRECT_CODEC, CelestialBody.DIRECT_CODEC);
        event.dataPackRegistry(AddonRegistries.GALAXY,
                Galaxy.DIRECT_CODEC, Galaxy.DIRECT_CODEC);
        event.dataPackRegistry(AddonRegistries.CELESTIAL_TELEPORTER,
                CelestialTeleporter.DIRECT_CODEC, CelestialTeleporter.DIRECT_CODEC);

        event.dataPackRegistry(RocketRegistries.TRAVEL_PREDICATE,
                ConfiguredTravelPredicate.DIRECT_CODEC, ConfiguredTravelPredicate.DIRECT_CODEC);
        event.dataPackRegistry(RocketRegistries.ROCKET_CONE,
                RocketCone.DIRECT_CODEC, RocketCone.DIRECT_CODEC);
        event.dataPackRegistry(RocketRegistries.ROCKET_BODY,
                RocketBody.DIRECT_CODEC, RocketBody.DIRECT_CODEC);
        event.dataPackRegistry(RocketRegistries.ROCKET_FIN,
                RocketFin.DIRECT_CODEC, RocketFin.DIRECT_CODEC);
        event.dataPackRegistry(RocketRegistries.ROCKET_BOOSTER,
                RocketBooster.DIRECT_CODEC, RocketBooster.DIRECT_CODEC);
        event.dataPackRegistry(RocketRegistries.ROCKET_BOTTOM,
                RocketBottom.DIRECT_CODEC, RocketBottom.DIRECT_CODEC);
        event.dataPackRegistry(RocketRegistries.ROCKET_UPGRADE,
                RocketUpgrade.DIRECT_CODEC, RocketUpgrade.DIRECT_CODEC);
        event.dataPackRegistry(RocketRegistries.ROCKET_PART_RECIPE,
                RocketPartRecipe.DIRECT_CODEC, RocketPartRecipe.DIRECT_CODEC);
    }

    /**
     * Loader-native replacements for the direct vanilla registrations performed by
     * the Fabric API initializer. Forge's serializer registry keeps modded entity
     * data IDs synchronized between logical sides.
     */
    private void registerForgeAndVanillaEntries(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR, Constant.id("satellite"),
                () -> SatelliteChunkGenerator.CODEC);

        event.register(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS,
                Constant.id("launch_stage"), () -> GCEntityDataSerializers.LAUNCH_STAGE);
        event.register(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS,
                Constant.id("rocket_part"), () -> GCEntityDataSerializers.ROCKET_PART);
        event.register(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS,
                Constant.id("rocket_upgrades"), () -> GCEntityDataSerializers.ROCKET_UPGRADES);
    }
}
