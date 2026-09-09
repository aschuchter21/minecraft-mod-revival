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

    public static final RegistryObject<Item> MISSION_CONTROL_CONSOLE = blockItem("mission_control_console", OIBlocks.MISSION_CONTROL_CONSOLE);
    public static final RegistryObject<Item> STATION_CONTROLLER = blockItem("station_controller", OIBlocks.STATION_CONTROLLER);
    public static final RegistryObject<Item> HABITATION_MODULE = blockItem("habitation_module", OIBlocks.HABITATION_MODULE);
    public static final RegistryObject<Item> LIFE_SUPPORT_MODULE = blockItem("life_support_module", OIBlocks.LIFE_SUPPORT_MODULE);
    public static final RegistryObject<Item> DOCKING_PORT = blockItem("docking_port", OIBlocks.DOCKING_PORT);
    public static final RegistryObject<Item> SOLAR_ARRAY = blockItem("solar_array", OIBlocks.SOLAR_ARRAY);
    public static final RegistryObject<Item> CARGO_STORAGE_MODULE = blockItem("cargo_storage_module", OIBlocks.CARGO_STORAGE_MODULE);

    private OIItems() {
    }

    private static RegistryObject<Item> blockItem(String name, RegistryObject<? extends net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
