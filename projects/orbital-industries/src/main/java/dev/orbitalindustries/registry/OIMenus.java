package dev.orbitalindustries.registry;

import dev.orbitalindustries.OrbitalIndustries;
import dev.orbitalindustries.menu.MissionControlMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OIMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, OrbitalIndustries.MOD_ID);

    public static final RegistryObject<MenuType<MissionControlMenu>> MISSION_CONTROL = MENUS.register("mission_control",
            () -> IForgeMenuType.create((windowId, inventory, data) ->
                    new MissionControlMenu(windowId, inventory, data.readBlockPos())));

    private OIMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
