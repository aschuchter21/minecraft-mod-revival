/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.forge;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.client.GCKeyBinds;
import dev.galacticraft.mod.client.gui.overlay.RocketOverlay;
import dev.galacticraft.mod.client.gui.overlay.OxygenOverlay;
import dev.galacticraft.mod.client.gui.overlay.LanderOverlay;
import dev.galacticraft.mod.client.gui.overlay.CountdownOverlay;
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
import dev.galacticraft.mod.client.model.entity.ArchGreyEntityModel;
import dev.galacticraft.mod.client.model.entity.CometCubeEntityModel;
import dev.galacticraft.mod.client.model.entity.EvolvedCreeperEntityModel;
import dev.galacticraft.mod.client.model.entity.GazerEntityModel;
import dev.galacticraft.mod.client.model.entity.GreyEntityModel;
import dev.galacticraft.mod.client.model.entity.LanderModel;
import dev.galacticraft.mod.client.model.entity.OliGrubEntityModel;
import dev.galacticraft.mod.client.model.entity.RumblerEntityModel;
import dev.galacticraft.mod.client.render.block.entity.AdvancedSolarPanelBlockEntityRenderer;
import dev.galacticraft.mod.client.render.block.entity.BasicSolarPanelBlockEntityRenderer;
import dev.galacticraft.mod.client.render.block.entity.FluidPipeBlockEntityRenderer;
import dev.galacticraft.mod.client.render.entity.BubbleEntityRenderer;
import dev.galacticraft.mod.client.render.entity.CometCubeEntityRenderer;
import dev.galacticraft.mod.client.render.entity.EvolvedCreeperEntityRenderer;
import dev.galacticraft.mod.client.render.entity.EvolvedEvokerEntityRenderer;
import dev.galacticraft.mod.client.render.entity.EvolvedPillagerEntityRenderer;
import dev.galacticraft.mod.client.render.entity.EvolvedSkeletonEntityRenderer;
import dev.galacticraft.mod.client.render.entity.EvolvedSpiderEntityRenderer;
import dev.galacticraft.mod.client.render.entity.EvolvedVindicatorEntityRenderer;
import dev.galacticraft.mod.client.render.entity.EvolvedZombieRenderer;
import dev.galacticraft.mod.client.render.entity.GazerEntityRenderer;
import dev.galacticraft.mod.client.render.entity.GreyEntityRenderer;
import dev.galacticraft.mod.client.render.entity.LanderEntityRenderer;
import dev.galacticraft.mod.client.render.entity.OliGrubEntityRenderer;
import dev.galacticraft.mod.client.render.entity.RumblerEntityRenderer;
import dev.galacticraft.mod.client.render.entity.rocket.RocketEntityRenderer;
import dev.galacticraft.mod.client.render.entity.model.GCEntityModelLayer;
import dev.galacticraft.mod.client.render.rocket.GalacticraftRocketPartRenderers;
import dev.galacticraft.mod.client.particle.CryoFreezeParticle;
import dev.galacticraft.mod.client.particle.DrippingFuelProvider;
import dev.galacticraft.mod.client.particle.DrippingOilProvider;
import dev.galacticraft.mod.client.particle.FallingCrudeOilProvider;
import dev.galacticraft.mod.client.particle.FallingFuelProvider;
import dev.galacticraft.mod.client.particle.LanderParticle;
import dev.galacticraft.mod.content.GCBlockEntityTypes;
import dev.galacticraft.mod.content.GCBlocks;
import dev.galacticraft.mod.content.GCEntityTypes;
import dev.galacticraft.mod.events.ClientEventHandler;
import dev.galacticraft.mod.content.entity.RocketEntity;
import dev.galacticraft.mod.content.entity.LanderEntity;
import dev.galacticraft.mod.forge.network.ForgeRocketNetworking;
import dev.galacticraft.mod.particle.GCParticleTypes;
import dev.galacticraft.mod.screen.GCMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
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
        modBus.addListener(GalacticraftForgeClient::registerRenderers);
        modBus.addListener(GalacticraftForgeClient::registerLayerDefinitions);
        modBus.addListener(GalacticraftForgeClient::registerKeyMappings);
        modBus.addListener(GalacticraftForgeClient::registerParticleProviders);
        modBus.addListener(GalacticraftForgeClient::registerDimensionEffects);
        modBus.addListener(GalacticraftForgeClient::registerGuiOverlays);
        MinecraftForge.EVENT_BUS.addListener(GalacticraftForgeClient::clientTick);
        modBus.addListener(GalacticraftRocketPartRenderers::registerAdditionalModels);
    }



    private static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(Constant.id("moon"), ForgeMoonDimensionEffects.INSTANCE);
    }

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("oxygen_tanks", (gui, graphics, partialTick, width, height) -> {
            OxygenOverlay.onHudRender(graphics, partialTick);
        });
        event.registerAboveAll("rocket_altitude", (gui, graphics, partialTick, width, height) -> {
            if (Minecraft.getInstance().player != null) {
                RocketOverlay.onHudRender(graphics, partialTick);
            }
        });
        event.registerAboveAll("lander_status", (gui, graphics, partialTick, width, height) -> {
            if (Minecraft.getInstance().player != null) {
                LanderOverlay.onRenderHud(graphics, partialTick);
            }
        });
        event.registerAboveAll("rocket_countdown", (gui, graphics, partialTick, width, height) -> {
            if (Minecraft.getInstance().player != null) {
                CountdownOverlay.renderCountdown(graphics, partialTick);
            }
        });
    }

    private static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        LanderOverlay.clientTick(minecraft);

        if (minecraft.player == null || minecraft.level == null) return;
        if (!(minecraft.player.getVehicle() instanceof RocketEntity)
                && !(minecraft.player.getVehicle() instanceof LanderEntity)) return;

        int flags = 0;
        if (minecraft.player.input.jumping) flags |= ForgeRocketNetworking.JUMP;
        if (minecraft.player.input.up) flags |= ForgeRocketNetworking.UP;
        if (minecraft.player.input.down) flags |= ForgeRocketNetworking.DOWN;
        if (minecraft.player.input.left) flags |= ForgeRocketNetworking.LEFT;
        if (minecraft.player.input.right) flags |= ForgeRocketNetworking.RIGHT;
        if (minecraft.player.input.shiftKeyDown) flags |= ForgeRocketNetworking.SNEAK;
        ForgeRocketNetworking.sendVehicleInput(flags);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(GCKeyBinds.OPEN_ROCKET_INVENTORY);
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(GCParticleTypes.DRIPPING_FUEL, DrippingFuelProvider::new);
        event.registerSpriteSet(GCParticleTypes.FALLING_FUEL, FallingFuelProvider::new);
        event.registerSpriteSet(GCParticleTypes.DRIPPING_CRUDE_OIL, DrippingOilProvider::new);
        event.registerSpriteSet(GCParticleTypes.FALLING_CRUDE_OIL, FallingCrudeOilProvider::new);
        event.registerSpriteSet(GCParticleTypes.CRYOGENIC_PARTICLE, CryoFreezeParticle.Provider::new);
        event.registerSpriteSet(GCParticleTypes.LANDER_FLAME_PARTICLE, LanderParticle.Provider::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GCEntityTypes.EVOLVED_ZOMBIE, EvolvedZombieRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.EVOLVED_CREEPER, EvolvedCreeperEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.EVOLVED_SKELETON, EvolvedSkeletonEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.EVOLVED_SPIDER, EvolvedSpiderEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.EVOLVED_EVOKER, EvolvedEvokerEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.EVOLVED_PILLAGER, EvolvedPillagerEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.EVOLVED_VINDICATOR, EvolvedVindicatorEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.GAZER, GazerEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.RUMBLER, RumblerEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.COMET_CUBE, CometCubeEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.OLI_GRUB, OliGrubEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.GREY, GreyEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.ARCH_GREY, GreyEntityRenderer::arch);
        event.registerEntityRenderer(GCEntityTypes.BUBBLE, BubbleEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.ROCKET, RocketEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.LANDER, LanderEntityRenderer::new);

        event.registerBlockEntityRenderer(GCBlockEntityTypes.BASIC_SOLAR_PANEL, BasicSolarPanelBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(GCBlockEntityTypes.ADVANCED_SOLAR_PANEL, AdvancedSolarPanelBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(GCBlockEntityTypes.GLASS_FLUID_PIPE, FluidPipeBlockEntityRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GCEntityModelLayer.EVOLVED_CREEPER,
                () -> EvolvedCreeperEntityModel.getTexturedModelData(CubeDeformation.NONE));
        event.registerLayerDefinition(GCEntityModelLayer.EVOLVED_CREEPER_ARMOR,
                () -> EvolvedCreeperEntityModel.getTexturedModelData(new CubeDeformation(2.0f)));
        event.registerLayerDefinition(GCEntityModelLayer.GAZER, GazerEntityModel::createBodyLayer);
        event.registerLayerDefinition(GCEntityModelLayer.RUMBLER, RumblerEntityModel::createBodyLayer);
        event.registerLayerDefinition(GCEntityModelLayer.COMET_CUBE, CometCubeEntityModel::createBodyLayer);
        event.registerLayerDefinition(GCEntityModelLayer.OLI_GRUB, OliGrubEntityModel::createBodyLayer);
        event.registerLayerDefinition(GCEntityModelLayer.GREY, GreyEntityModel::createBodyLayer);
        event.registerLayerDefinition(GCEntityModelLayer.ARCH_GREY, ArchGreyEntityModel::createBodyLayer);
        event.registerLayerDefinition(GCEntityModelLayer.LANDER, LanderModel::createBodyLayer);
        event.registerLayerDefinition(GCEntityModelLayer.SOLAR_PANEL, BasicSolarPanelBlockEntityRenderer::getTexturedModelData);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientEventHandler.init();
            GalacticraftRocketPartRenderers.register();

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
