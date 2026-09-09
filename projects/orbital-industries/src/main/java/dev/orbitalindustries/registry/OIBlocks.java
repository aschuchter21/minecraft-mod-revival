package dev.orbitalindustries.registry;

import dev.orbitalindustries.OrbitalIndustries;
import dev.orbitalindustries.content.block.MissionControlBlock;
import dev.orbitalindustries.content.block.StationControllerBlock;
import dev.orbitalindustries.content.block.StationModuleBlock;
import dev.orbitalindustries.station.StationModuleType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OIBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, OrbitalIndustries.MOD_ID);

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(4.0F, 8.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    public static final RegistryObject<Block> MISSION_CONTROL_CONSOLE = BLOCKS.register("mission_control_console",
            () -> new MissionControlBlock(machineProperties()));

    public static final RegistryObject<Block> STATION_CONTROLLER = BLOCKS.register("station_controller",
            () -> new StationControllerBlock(machineProperties()));

    public static final RegistryObject<Block> HABITATION_MODULE = BLOCKS.register("habitation_module",
            () -> new StationModuleBlock(StationModuleType.HABITATION, machineProperties()));

    public static final RegistryObject<Block> LIFE_SUPPORT_MODULE = BLOCKS.register("life_support_module",
            () -> new StationModuleBlock(StationModuleType.LIFE_SUPPORT, machineProperties()));

    public static final RegistryObject<Block> DOCKING_PORT = BLOCKS.register("docking_port",
            () -> new StationModuleBlock(StationModuleType.DOCKING_PORT, machineProperties()));

    public static final RegistryObject<Block> SOLAR_ARRAY = BLOCKS.register("solar_array",
            () -> new StationModuleBlock(StationModuleType.SOLAR_ARRAY, machineProperties()));

    public static final RegistryObject<Block> CARGO_STORAGE_MODULE = BLOCKS.register("cargo_storage_module",
            () -> new StationModuleBlock(StationModuleType.CARGO_STORAGE, machineProperties()));

    private OIBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
