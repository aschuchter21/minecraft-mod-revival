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

/** Forge 1.20.1 crude-oil fluid. */
public abstract class CrudeOilFluid extends BasicFluid {
    protected CrudeOilFluid() {
        super(false, true, 2, 1, 30, 100);
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return ForgeGCFluidTypes.CRUDE_OIL;
    }

    @Override
    public Fluid getFlowing() {
        return GCFluids.FLOWING_CRUDE_OIL;
    }

    @Override
    public Fluid getSource() {
        return GCFluids.CRUDE_OIL;
    }

    @Override
    public ParticleOptions getDripParticle() {
        return GCParticleTypes.DRIPPING_CRUDE_OIL;
    }

    @Override
    public Item getBucket() {
        return GCItems.CRUDE_OIL_BUCKET;
    }

    @Override
    protected LiquidBlock getBlock() {
        return GCBlocks.CRUDE_OIL;
    }

    public static class Still extends CrudeOilFluid {
        @Override
        public boolean isStill() {
            return true;
        }
    }

    public static class Flowing extends CrudeOilFluid {
        @Override
        public boolean isStill() {
            return false;
        }
    }
}
