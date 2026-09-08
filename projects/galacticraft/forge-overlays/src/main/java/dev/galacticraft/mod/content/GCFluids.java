/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.fluid.CrudeOilFluid;
import dev.galacticraft.mod.content.fluid.FuelFluid;
import dev.galacticraft.mod.content.fluid.OxygenFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegisterEvent;

/** Forge lifecycle-backed fluid registry facade. */
public final class GCFluids {
    public static final FlowingFluid CRUDE_OIL = new CrudeOilFluid.Still();
    public static final FlowingFluid FLOWING_CRUDE_OIL = new CrudeOilFluid.Flowing();
    public static final FlowingFluid FUEL = new FuelFluid.Still();
    public static final FlowingFluid FLOWING_FUEL = new FuelFluid.Flowing();
    public static final Fluid LIQUID_OXYGEN = new OxygenFluid();

    private GCFluids() {
    }

    /** Original common entrypoint retained for binary/source compatibility. */
    public static void register() {
        // Forge owns registration timing through RegisterEvent.
    }

    /** Forge registration entrypoint used by GalacticraftForgeBootstrap. */
    public static void register(RegisterEvent event) {
        register(event, Constant.Fluid.CRUDE_OIL_STILL, CRUDE_OIL);
        register(event, Constant.Fluid.CRUDE_OIL_FLOWING, FLOWING_CRUDE_OIL);
        register(event, Constant.Fluid.FUEL_STILL, FUEL);
        register(event, Constant.Fluid.FUEL_FLOWING, FLOWING_FUEL);
        register(event, Constant.Fluid.LIQUID_OXYGEN, LIQUID_OXYGEN);
    }

    /** Fabric FluidVariant attributes are represented by Forge FluidTypes on this loader. */
    public static void registerFluidVariantAttributes() {
    }

    private static void register(RegisterEvent event, String id, Fluid fluid) {
        ResourceLocation name = new ResourceLocation(Constant.MOD_ID, id);
        event.register(Registries.FLUID, name, () -> fluid);
    }
}
