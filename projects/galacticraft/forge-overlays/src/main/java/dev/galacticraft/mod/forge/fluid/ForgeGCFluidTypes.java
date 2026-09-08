/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.fluid;

import dev.galacticraft.mod.Constant;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Forge 1.20.1 FluidTypes for Galacticraft's placeable/storage liquids. */
public final class ForgeGCFluidTypes {
    public static final FluidType CRUDE_OIL = new FluidType(FluidType.Properties.create()
            .descriptionId("block.galacticraft.crude_oil")
            .density(1000)
            .viscosity(6000)) {};

    public static final FluidType FUEL = new FluidType(FluidType.Properties.create()
            .descriptionId("block.galacticraft.fuel")
            .density(1000)
            .viscosity(2000)) {};

    public static final FluidType LIQUID_OXYGEN = new FluidType(FluidType.Properties.create()
            .descriptionId("block.galacticraft.oxygen")
            .density(-100)
            .viscosity(500)
            .canDrown(false)) {};

    private ForgeGCFluidTypes() {
    }

    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.FLUID_TYPES, Constant.id("crude_oil"), () -> CRUDE_OIL);
        event.register(ForgeRegistries.Keys.FLUID_TYPES, Constant.id("fuel"), () -> FUEL);
        event.register(ForgeRegistries.Keys.FLUID_TYPES, Constant.id("liquid_oxygen"), () -> LIQUID_OXYGEN);
    }
}
