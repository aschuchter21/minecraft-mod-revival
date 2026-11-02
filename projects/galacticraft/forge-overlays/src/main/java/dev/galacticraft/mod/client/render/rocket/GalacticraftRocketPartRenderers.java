/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.client.render.rocket;

import com.google.common.base.Suppliers;
import dev.galacticraft.api.entity.rocket.render.RocketPartRendererRegistry;
import dev.galacticraft.impl.client.rocket.render.BakedModelRocketPartRenderer;
import dev.galacticraft.impl.universe.BuiltinObjects;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.GCRocketParts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.ModelEvent;

import java.util.Objects;

/**
 * Forge-native rocket-part model registration.
 *
 * The original 1.20.1 implementation used Fabric's ModelLoadingRegistry and
 * BakedModelManagerHelper. Forge's RegisterAdditional event and vanilla
 * ModelManager provide the same lifecycle without a Fabric runtime dependency.
 */
public final class GalacticraftRocketPartRenderers {
    private static final ResourceLocation DEFAULT_CONE = new ResourceLocation(Constant.MOD_ID, "misc/rocket_cone_basic");
    private static final ResourceLocation ADVANCED_CONE = new ResourceLocation(Constant.MOD_ID, "misc/rocket_cone_advanced");
    private static final ResourceLocation SLOPED_CONE = new ResourceLocation(Constant.MOD_ID, "misc/rocket_cone_sloped");
    private static final ResourceLocation DEFAULT_BODY = new ResourceLocation(Constant.MOD_ID, "misc/rocket_body");
    private static final ResourceLocation DEFAULT_FIN = new ResourceLocation(Constant.MOD_ID, "misc/rocket_fins");
    private static final ResourceLocation DEFAULT_BOTTOM = new ResourceLocation(Constant.MOD_ID, "misc/rocket_bottom");
    private static final ResourceLocation BOOSTER_TIER_1 = new ResourceLocation(Constant.MOD_ID, "misc/rocket_thruster_tier_1");
    private static final ResourceLocation BOOSTER_TIER_2 = new ResourceLocation(Constant.MOD_ID, "misc/rocket_thruster_tier_2");

    private GalacticraftRocketPartRenderers() {
    }

    public static void register() {
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.TIER_1_CONE,
                new BakedModelRocketPartRenderer(Suppliers.memoize(() -> model(DEFAULT_CONE)), Sheets::translucentCullBlockSheet));
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.ADVANCED_CONE,
                new BakedModelRocketPartRenderer(Suppliers.memoize(() -> model(ADVANCED_CONE)), Sheets::translucentCullBlockSheet));
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.SLOPED_CONE,
                new BakedModelRocketPartRenderer(Suppliers.memoize(() -> model(SLOPED_CONE)), Sheets::translucentCullBlockSheet));
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.TIER_1_BODY,
                new BakedModelRocketPartRenderer(Suppliers.memoize(() -> model(DEFAULT_BODY)), Sheets::translucentCullBlockSheet));
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.TIER_1_FIN,
                new BakedModelRocketPartRenderer(Suppliers.memoize(() -> model(DEFAULT_FIN)), Sheets::translucentCullBlockSheet));
        RocketPartRendererRegistry.INSTANCE.register(BuiltinObjects.INVALID_ROCKET_UPGRADE,
                new BakedModelItemRocketPartRenderer(Items.BARRIER.getDefaultInstance(), null));
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.TIER_1_BOTTOM,
                new BakedModelRocketPartRenderer(Suppliers.memoize(() -> model(DEFAULT_BOTTOM)), Sheets::translucentCullBlockSheet));
        RocketPartRendererRegistry.INSTANCE.register(BuiltinObjects.INVALID_ROCKET_BOOSTER,
                new BakedModelItemRocketPartRenderer(Items.BARRIER.getDefaultInstance(), null));
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.STORAGE_UPGRADE,
                new BakedModelItemRocketPartRenderer(Items.CHEST.getDefaultInstance(), null));
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.TIER_1_BOOSTER,
                new BakedModelRocketPartRenderer(Suppliers.memoize(() -> model(BOOSTER_TIER_1)), Sheets::translucentCullBlockSheet));
        RocketPartRendererRegistry.INSTANCE.register(GCRocketParts.TIER_2_BOOSTER,
                new BakedModelRocketPartRenderer(Suppliers.memoize(() -> model(BOOSTER_TIER_2)), Sheets::translucentCullBlockSheet));
    }

    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(DEFAULT_CONE);
        event.register(SLOPED_CONE);
        event.register(ADVANCED_CONE);
        event.register(DEFAULT_BODY);
        event.register(DEFAULT_BOTTOM);
        event.register(DEFAULT_FIN);
        event.register(BOOSTER_TIER_1);
        event.register(BOOSTER_TIER_2);
    }

    /** Kept for recovered-call-site ABI; Forge registration happens through the event above. */
    public static void registerModelLoader() {
    }

    private static net.minecraft.client.resources.model.BakedModel model(ResourceLocation id) {
        return Objects.requireNonNull(Minecraft.getInstance().getModelManager().getModel(id));
    }
}
