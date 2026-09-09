package dev.orbitalindustries.client;

import dev.orbitalindustries.OrbitalIndustries;
import dev.orbitalindustries.client.screen.MissionControlScreen;
import dev.orbitalindustries.registry.OIMenus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OrbitalIndustries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class OIClientEvents {
    private OIClientEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(OIMenus.MISSION_CONTROL.get(), MissionControlScreen::new);
    }
}
