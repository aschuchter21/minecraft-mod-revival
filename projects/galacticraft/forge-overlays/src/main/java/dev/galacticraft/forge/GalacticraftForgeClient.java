/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.forge;

import dev.galacticraft.mod.client.gui.screen.ingame.AdvancedSolarPanelScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.AirlockControllerScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.BasicSolarPanelScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.BubbleDistributorScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.CircuitFabricatorScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.CoalGeneratorScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.CompressorScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.ElectricArcFurnaceScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.ElectricCompressorScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.ElectricFurnaceScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.EnergyStorageModuleScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.FuelLoaderScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.GCPlayerInventoryScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.OxygenCollectorScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.OxygenCompressorScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.OxygenDecompressorScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.OxygenSealerScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.OxygenStorageModuleScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.RefineryScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.RocketInventoryScreen;
import dev.galacticraft.mod.client.gui.screen.ingame.RocketWorkbenchScreen;
import dev.galacticraft.mod.content.GCBlocks;
import dev.galacticraft.mod.screen.GCMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Forge-native client bootstrap.
 *
 * Keep this class free of Fabric client APIs. Client functionality is being
 * restored in small slices so the dedicated-server runtime remains load-safe.
 */
public final class GalacticraftForgeClient {
    private GalacticraftForgeClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(GalacticraftForgeClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(GCMenuTypes.BASIC_SOLAR_PANEL, BasicSolarPanelScreen::new);
            MenuScreens.register(GCMenuTypes.ADVANCED_SOLAR_PANEL, AdvancedSolarPanelScreen::new);
            MenuScreens.register(GCMenuTypes.COAL_GENERATOR, CoalGeneratorScreen::new);
            MenuScreens.register(GCMenuTypes.CIRCUIT_FABRICATOR, CircuitFabricatorScreen::new);
            MenuScreens.register(GCMenuTypes.REFINERY, RefineryScreen::new);
            MenuScreens.register(GCMenuTypes.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
            MenuScreens.register(GCMenuTypes.ELECTRIC_ARC_FURNACE, ElectricArcFurnaceScreen::new);
            MenuScreens.register(GCMenuTypes.COMPRESSOR, CompressorScreen::new);
            MenuScreens.register(GCMenuTypes.ELECTRIC_COMPRESSOR, ElectricCompressorScreen::new);
            MenuScreens.register(GCMenuTypes.ENERGY_STORAGE_MODULE, EnergyStorageModuleScreen::new);
            MenuScreens.register(GCMenuTypes.OXYGEN_COLLECTOR, OxygenCollectorScreen::new);
            MenuScreens.register(GCMenuTypes.OXYGEN_COMPRESSOR, OxygenCompressorScreen::new);
            MenuScreens.register(GCMenuTypes.OXYGEN_DECOMPRESSOR, OxygenDecompressorScreen::new);
            MenuScreens.register(GCMenuTypes.PLAYER_INV_GC, GCPlayerInventoryScreen::new);
            MenuScreens.register(GCMenuTypes.OXYGEN_BUBBLE_DISTRIBUTOR, BubbleDistributorScreen::new);
            MenuScreens.register(GCMenuTypes.OXYGEN_STORAGE_MODULE, OxygenStorageModuleScreen::new);
            MenuScreens.register(GCMenuTypes.OXYGEN_SEALER, OxygenSealerScreen::new);
            MenuScreens.register(GCMenuTypes.FUEL_LOADER, FuelLoaderScreen::new);
            MenuScreens.register(GCMenuTypes.AIRLOCK_CONTROLLER_MENU, AirlockControllerScreen::new);
            MenuScreens.register(GCMenuTypes.ROCKET_WORKBENCH, RocketWorkbenchScreen::new);
            MenuScreens.register(GCMenuTypes.ROCKET, RocketInventoryScreen::new);

            ItemBlockRenderTypes.setRenderLayer(GCBlocks.TIN_LADDER, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.GLASS_FLUID_PIPE, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.WALKWAY, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.WIRE_WALKWAY, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.FLUID_PIPE_WALKWAY, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.MOON_BERRY_BUSH, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.GLOWSTONE_TORCH, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.GLOWSTONE_WALL_TORCH, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.UNLIT_TORCH, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.UNLIT_WALL_TORCH, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.GLOWSTONE_LANTERN, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.UNLIT_LANTERN, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.CAVERNOUS_VINES, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.CAVERNOUS_VINES_PLANT, RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(GCBlocks.CRYOGENIC_CHAMBER, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.CRYOGENIC_CHAMBER_PART, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(GCBlocks.PLAYER_TRANSPORT_TUBE, RenderType.translucent());
        });
    }
}
