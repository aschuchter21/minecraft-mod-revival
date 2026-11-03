/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.forge;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.world.biome.source.GCMultiNoiseBiomeSourceParameterLists;
import dev.galacticraft.mod.world.gen.carver.GCCarvers;
import dev.galacticraft.mod.world.gen.structure.GCStructureTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraftforge.registries.RegisterEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Restores the worldgen codecs that the original Fabric initializer registered
 * before Galacticraft's generated Moon datapack definitions were read.
 *
 * <p>Do not remove the generated worldgen resources to silence datapack errors:
 * the carvers and ruins are part of the Moon terrain and exploration loop.</p>
 */
public final class GalacticraftForgeWorldgen {
    private GalacticraftForgeWorldgen() {
    }

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.CARVER)) {
            event.register(Registries.CARVER, Constant.id("lunar_cave"), () -> GCCarvers.LUNAR_CAVE);
            event.register(Registries.CARVER, Constant.id("craters"), () -> GCCarvers.CRATERS);
        }

        if (event.getRegistryKey().equals(Registries.STRUCTURE_TYPE)) {
            event.register(Registries.STRUCTURE_TYPE, Constant.id("moon_ruins"),
                    () -> GCStructureTypes.MOON_RUINS);
        }
    }

    /**
     * Mojang stores multi-noise presets in an internal map rather than a Forge
     * registry. The original version injects into that map with a Fabric mixin.
     * Here we locate the map by its known vanilla contents, not its obfuscated
     * private field name, so this works in both Mojmap dev and Forge production.
     */
    @SuppressWarnings("unchecked")
    public static void installMoonBiomePreset() {
        ResourceLocation moonId = GCMultiNoiseBiomeSourceParameterLists.MOON_PRESET_ID;
        ResourceLocation vanillaId = MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.id();

        for (Field field : MultiNoiseBiomeSourceParameterList.Preset.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Map.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(null);
                if (!(value instanceof Map<?, ?> raw) || !raw.containsKey(vanillaId)) {
                    continue;
                }

                Map<ResourceLocation, MultiNoiseBiomeSourceParameterList.Preset> presets =
                        (Map<ResourceLocation, MultiNoiseBiomeSourceParameterList.Preset>) raw;
                MultiNoiseBiomeSourceParameterList.Preset existing =
                        presets.putIfAbsent(moonId, GCMultiNoiseBiomeSourceParameterLists.MOON_PRESET);
                if (existing != null && existing != GCMultiNoiseBiomeSourceParameterLists.MOON_PRESET) {
                    throw new IllegalStateException("Conflicting Galacticraft Moon biome preset: " + moonId);
                }
                return;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Cannot access vanilla biome preset map", exception);
            }
        }

        throw new IllegalStateException("Cannot locate vanilla multi-noise biome preset map");
    }
}
