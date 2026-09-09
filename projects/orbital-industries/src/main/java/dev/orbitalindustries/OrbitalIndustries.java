package dev.orbitalindustries;

import dev.orbitalindustries.registry.OIBlockEntities;
import dev.orbitalindustries.registry.OIBlocks;
import dev.orbitalindustries.registry.OIItems;
import dev.orbitalindustries.registry.OIMenus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OrbitalIndustries.MOD_ID)
public final class OrbitalIndustries {
    public static final String MOD_ID = "orbitalindustries";

    public OrbitalIndustries() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        OIBlocks.register(modBus);
        OIItems.register(modBus);
        OIBlockEntities.register(modBus);
        OIMenus.register(modBus);
    }
}
