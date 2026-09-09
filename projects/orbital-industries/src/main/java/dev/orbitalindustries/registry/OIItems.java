package dev.orbitalindustries.registry;

import dev.orbitalindustries.OrbitalIndustries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OIItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, OrbitalIndustries.MOD_ID);

    public static final RegistryObject<Item> MISSION_CONTROL_CONSOLE = ITEMS.register("mission_control_console",
            () -> new BlockItem(OIBlocks.MISSION_CONTROL_CONSOLE.get(), new Item.Properties()));

    private OIItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
