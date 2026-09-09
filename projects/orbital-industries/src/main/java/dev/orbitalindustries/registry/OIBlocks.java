package dev.orbitalindustries.registry;

import dev.orbitalindustries.OrbitalIndustries;
import dev.orbitalindustries.content.block.MissionControlBlock;
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

    public static final RegistryObject<Block> MISSION_CONTROL_CONSOLE = BLOCKS.register("mission_control_console",
            () -> new MissionControlBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 8.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    private OIBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
