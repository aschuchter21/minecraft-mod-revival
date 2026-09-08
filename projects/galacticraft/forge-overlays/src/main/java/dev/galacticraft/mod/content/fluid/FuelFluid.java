/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.fluid;

import dev.galacticraft.mod.content.GCBlocks;
import dev.galacticraft.mod.content.GCFluids;
import dev.galacticraft.mod.content.item.GCItems;
import dev.galacticraft.mod.forge.fluid.ForgeGCFluidTypes;
import dev.galacticraft.mod.particle.GCParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

/** Forge 1.20.1 rocket-fuel fluid. */
public abstract class FuelFluid extends BasicFluid {
    public FuelFluid() {
        super(false, true, 3, 1, 10, 100);
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ForgeGCFluidTypes.FUEL;
    }

    @Override
    public Fluid getFlowing() {
        return GCFluids.FLOWING_FUEL;
    }

    @Override
    public Fluid getSource() {
        return GCFluids.FUEL;
    }

    @Override
    public ParticleOptions getDripParticle() {
        return GCParticleTypes.DRIPPING_FUEL;
    }

    @Override
    public Item getBucket() {
        return GCItems.FUEL_BUCKET;
    }

    @Override
    protected LiquidBlock getBlock() {
        return GCBlocks.FUEL;
    }

    public static class Still extends FuelFluid {
        @Override
        public boolean isStill() {
            return true;
        }
    }

    public static class Flowing extends FuelFluid {
        @Override
        public boolean isStill() {
            return false;
        }
    }
}
