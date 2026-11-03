/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.forge;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Forge client equivalent of Galacticraft's original MoonDimensionEffects.
 * Its black fog and dark sunrise transition are required for the Moon
 * dimension to have the original airless appearance.
 */
public final class ForgeMoonDimensionEffects extends DimensionSpecialEffects {
    public static final ForgeMoonDimensionEffects INSTANCE = new ForgeMoonDimensionEffects();
    private static final float[] NO_SUNRISE = {0.0F, 0.0F, 0.0F, 0.0F};

    private ForgeMoonDimensionEffects() {
        super(Float.NaN, false, SkyType.NORMAL, true, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int camX, int camY) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float skyAngle, float tickDelta) {
        return NO_SUNRISE;
    }
}
