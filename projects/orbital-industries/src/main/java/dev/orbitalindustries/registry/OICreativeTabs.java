package dev.orbitalindustries.registry;

import dev.orbitalindustries.OrbitalIndustries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OrbitalIndustries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OICreativeTabs {
    private OICreativeTabs() {
    }

    @SubscribeEvent
    public static void addItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(OIItems.MISSION_CONTROL_CONSOLE.get());
        }
    }
}
