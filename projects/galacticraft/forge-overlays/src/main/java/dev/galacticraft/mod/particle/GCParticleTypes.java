/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.particle;

import dev.galacticraft.mod.Constant;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

/** Forge/vanilla particle type definitions for the 1.20.1 port. */
public final class GCParticleTypes {
    public static final SimpleParticleType DRIPPING_FUEL = new SimpleParticleType(false);
    public static final SimpleParticleType FALLING_FUEL = new SimpleParticleType(false);
    public static final SimpleParticleType DRIPPING_CRUDE_OIL = new SimpleParticleType(false);
    public static final SimpleParticleType FALLING_CRUDE_OIL = new SimpleParticleType(false);
    public static final SimpleParticleType CRYOGENIC_PARTICLE = new SimpleParticleType(false);
    public static final SimpleParticleType LANDER_FLAME_PARTICLE = new SimpleParticleType(false);

    private GCParticleTypes() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Constant.id(Constant.Particle.DRIPPING_CRUDE_OIL), DRIPPING_CRUDE_OIL);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Constant.id(Constant.Particle.FALLING_CRUDE_OIL), FALLING_CRUDE_OIL);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Constant.id(Constant.Particle.DRIPPING_FUEL), DRIPPING_FUEL);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Constant.id(Constant.Particle.FALLING_FUEL), FALLING_FUEL);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Constant.id(Constant.Particle.CRYOGENIC_PARTICLE), CRYOGENIC_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Constant.id(Constant.Particle.LANDER_FLAME), LANDER_FLAME_PARTICLE);
    }
}
