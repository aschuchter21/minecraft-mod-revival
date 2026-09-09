package dev.orbitalindustries.registry;

import dev.orbitalindustries.OrbitalIndustries;
import dev.orbitalindustries.content.block.entity.MissionControlBlockEntity;
import dev.orbitalindustries.content.block.entity.StationControllerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class OIBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, OrbitalIndustries.MOD_ID);

    public static final RegistryObject<BlockEntityType<MissionControlBlockEntity>> MISSION_CONTROL =
            BLOCK_ENTITIES.register("mission_control", () -> BlockEntityType.Builder
                    .of(MissionControlBlockEntity::new, OIBlocks.MISSION_CONTROL_CONSOLE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<StationControllerBlockEntity>> STATION_CONTROLLER =
            BLOCK_ENTITIES.register("station_controller", () -> BlockEntityType.Builder
                    .of(StationControllerBlockEntity::new, OIBlocks.STATION_CONTROLLER.get())
                    .build(null));

    private OIBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
