/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.world.gen.structure;

import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Loader-neutral definition of Galacticraft's structure codec. On Forge the
 * actual registry insertion happens during RegisterEvent rather than during
 * this class's static initialization.
 */
public final class GCStructureTypes {
    public static final StructureType<MoonRuinsStructure> MOON_RUINS = () -> MoonRuinsStructure.CODEC;

    private GCStructureTypes() {
    }

    /** Compatibility entry point for original Galacticraft call sites. */
    public static void register() {
    }
}
