/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.api.gas;

import dev.galacticraft.mod.Constant;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/** Forge lifecycle-backed version of Galacticraft's built-in gas catalog. */
@ApiStatus.Experimental
@Deprecated
public final class Gases {
    public static final ResourceLocation HYDROGEN_ID = Constant.id("hydrogen");
    public static final Fluid HYDROGEN = gas(Constant.Text.TranslationKey.HYDROGEN, "hydrogen", "H2");
    public static final ResourceLocation NITROGEN_ID = Constant.id("nitrogen");
    public static final Fluid NITROGEN = gas(Constant.Text.TranslationKey.NITROGEN, "nitrogen", "N2");
    public static final ResourceLocation OXYGEN_ID = Constant.id("oxygen");
    public static final Fluid OXYGEN = gas(Constant.Text.TranslationKey.OXYGEN, "oxygen", "O2");
    public static final ResourceLocation CARBON_DIOXIDE_ID = Constant.id("carbon_dioxide");
    public static final Fluid CARBON_DIOXIDE = gas(Constant.Text.TranslationKey.CARBON_DIOXIDE, "carbon_dioxide", "CO2");
    public static final ResourceLocation WATER_VAPOR_ID = Constant.id("water_vapor");
    public static final Fluid WATER_VAPOR = gas(Constant.Text.TranslationKey.WATER_VAPOR, "water_vapor", "H2O");
    public static final ResourceLocation METHANE_ID = Constant.id("methane");
    public static final Fluid METHANE = gas(Constant.Text.TranslationKey.METHANE, "methane", "CH4");
    public static final ResourceLocation HELIUM_ID = Constant.id("helium");
    public static final Fluid HELIUM = gas(Constant.Text.TranslationKey.HELIUM, "helium", "He");
    public static final ResourceLocation ARGON_ID = Constant.id("argon");
    public static final Fluid ARGON = gas(Constant.Text.TranslationKey.ARGON, "argon", "Ar");
    public static final ResourceLocation NEON_ID = Constant.id("neon");
    public static final Fluid NEON = gas(Constant.Text.TranslationKey.NEON, "neon", "Ne");
    public static final ResourceLocation KRYPTON_ID = Constant.id("krypton");
    public static final Fluid KRYPTON = gas(Constant.Text.TranslationKey.KRYPTON, "krypton", "Kr");
    public static final ResourceLocation NITROUS_OXIDE_ID = Constant.id("nitrous_oxide");
    public static final Fluid NITROUS_OXIDE = gas(Constant.Text.TranslationKey.NITROUS_OXIDE, "nitrous_oxide", "N2O");
    public static final ResourceLocation CARBON_MONOXIDE_ID = Constant.id("carbon_monoxide");
    public static final Fluid CARBON_MONOXIDE = gas(Constant.Text.TranslationKey.CARBON_MONOXIDE, "carbon_monoxide", "CO");
    public static final ResourceLocation XENON_ID = Constant.id("xenon");
    public static final Fluid XENON = gas(Constant.Text.TranslationKey.XENON, "xenon", "Xe");
    public static final ResourceLocation OZONE_ID = Constant.id("ozone");
    public static final Fluid OZONE = gas(Constant.Text.TranslationKey.OZONE, "ozone", "O3");
    public static final ResourceLocation NITROUS_DIOXIDE_ID = Constant.id("nitrous_dioxide");
    public static final Fluid NITROUS_DIOXIDE = gas(Constant.Text.TranslationKey.NITROUS_DIOXIDE, "nitrous_dioxide", "NO2");
    public static final ResourceLocation IODINE_ID = Constant.id("iodine");
    public static final Fluid IODINE = gas(Constant.Text.TranslationKey.IODINE, "iodine", "I2");

    private Gases() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static GasFluid gas(String translationKey, String texture, String symbol) {
        return GasFluid.create(Component.translatable(translationKey), Constant.id("gas/" + texture), symbol);
    }

    /** Called for every Forge RegisterEvent; each registration self-filters by registry key. */
    public static void register(RegisterEvent event) {
        register(event, HYDROGEN_ID, HYDROGEN);
        register(event, NITROGEN_ID, NITROGEN);
        register(event, OXYGEN_ID, OXYGEN);
        register(event, CARBON_DIOXIDE_ID, CARBON_DIOXIDE);
        register(event, WATER_VAPOR_ID, WATER_VAPOR);
        register(event, METHANE_ID, METHANE);
        register(event, HELIUM_ID, HELIUM);
        register(event, ARGON_ID, ARGON);
        register(event, NEON_ID, NEON);
        register(event, KRYPTON_ID, KRYPTON);
        register(event, NITROUS_OXIDE_ID, NITROUS_OXIDE);
        register(event, CARBON_MONOXIDE_ID, CARBON_MONOXIDE);
        register(event, XENON_ID, XENON);
        register(event, OZONE_ID, OZONE);
        register(event, NITROUS_DIOXIDE_ID, NITROUS_DIOXIDE);
        register(event, IODINE_ID, IODINE);
    }

    private static void register(RegisterEvent event, ResourceLocation id, Fluid fluid) {
        GasFluid gas = (GasFluid) fluid;
        event.register(ForgeRegistries.Keys.FLUID_TYPES, id, gas::getFluidType);
        event.register(Registries.FLUID, id, () -> fluid);
    }

    @Contract(pure = true)
    public static void init() {
        // Forge registration is driven by RegisterEvent.
    }
}
