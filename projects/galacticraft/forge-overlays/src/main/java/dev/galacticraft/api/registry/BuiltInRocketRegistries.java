/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.api.registry;

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.api.rocket.part.type.RocketBodyType;
import dev.galacticraft.api.rocket.part.type.RocketBoosterType;
import dev.galacticraft.api.rocket.part.type.RocketBottomType;
import dev.galacticraft.api.rocket.part.type.RocketConeType;
import dev.galacticraft.api.rocket.part.type.RocketFinType;
import dev.galacticraft.api.rocket.part.type.RocketUpgradeType;
import dev.galacticraft.api.rocket.recipe.type.RocketPartRecipeType;
import dev.galacticraft.api.rocket.travelpredicate.TravelPredicateType;
import dev.galacticraft.impl.rocket.part.type.BasicRocketBodyType;
import dev.galacticraft.impl.rocket.part.type.BasicRocketBoosterType;
import dev.galacticraft.impl.rocket.part.type.BasicRocketBottomType;
import dev.galacticraft.impl.rocket.part.type.BasicRocketConeType;
import dev.galacticraft.impl.rocket.part.type.BasicRocketFinType;
import dev.galacticraft.impl.rocket.part.type.InvalidRocketBodyType;
import dev.galacticraft.impl.rocket.part.type.InvalidRocketBoosterType;
import dev.galacticraft.impl.rocket.part.type.InvalidRocketBottomType;
import dev.galacticraft.impl.rocket.part.type.InvalidRocketConeType;
import dev.galacticraft.impl.rocket.part.type.InvalidRocketFinType;
import dev.galacticraft.impl.rocket.part.type.InvalidRocketUpgradeType;
import dev.galacticraft.impl.rocket.part.type.NoUpgradeRocketUpgradeType;
import dev.galacticraft.impl.rocket.recipe.type.PatternedRocketPartRecipeType;
import dev.galacticraft.impl.rocket.travelpredicate.type.AccessWeightTravelPredicateType;
import dev.galacticraft.impl.rocket.travelpredicate.type.AndTravelPredicateType;
import dev.galacticraft.impl.rocket.travelpredicate.type.ConstantTravelPredicateType;
import dev.galacticraft.impl.rocket.travelpredicate.type.DefaultTravelPredicateType;
import dev.galacticraft.impl.rocket.travelpredicate.type.OrTravelPredicateType;
import dev.galacticraft.mod.Constant;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;

/** Forge-clean static registries used by Galacticraft's rocket codecs. */
public final class BuiltInRocketRegistries {
    public static final WritableRegistry<TravelPredicateType<?>> TRAVEL_PREDICATE_TYPE =
            new MappedRegistry<>(RocketRegistries.TRAVEL_PREDICATE_TYPE, Lifecycle.experimental(), true);

    public static final WritableRegistry<RocketConeType<?>> ROCKET_CONE_TYPE =
            defaulted(RocketRegistries.ROCKET_CONE_TYPE);
    public static final WritableRegistry<RocketBodyType<?>> ROCKET_BODY_TYPE =
            defaulted(RocketRegistries.ROCKET_BODY_TYPE);
    public static final WritableRegistry<RocketFinType<?>> ROCKET_FIN_TYPE =
            defaulted(RocketRegistries.ROCKET_FIN_TYPE);
    public static final WritableRegistry<RocketBoosterType<?>> ROCKET_BOOSTER_TYPE =
            defaulted(RocketRegistries.ROCKET_BOOSTER_TYPE);
    public static final WritableRegistry<RocketBottomType<?>> ROCKET_BOTTOM_TYPE =
            defaulted(RocketRegistries.ROCKET_BOTTOM_TYPE);
    public static final WritableRegistry<RocketUpgradeType<?>> ROCKET_UPGRADE_TYPE =
            defaulted(RocketRegistries.ROCKET_UPGRADE_TYPE);

    public static final WritableRegistry<RocketPartRecipeType<?>> ROCKET_PART_RECIPE_TYPE =
            new DefaultedMappedRegistry<>(
                    Constant.id("slotted_default").toString(),
                    RocketRegistries.ROCKET_PART_RECIPE_TYPE,
                    Lifecycle.experimental(),
                    false
            );

    private BuiltInRocketRegistries() {
    }

    private static <T> WritableRegistry<T> defaulted(net.minecraft.resources.ResourceKey<? extends Registry<T>> key) {
        return new DefaultedMappedRegistry<>(
                Constant.Misc.INVALID.toString(),
                key,
                Lifecycle.experimental(),
                false
        );
    }

    public static void initialize() {
        // Class initialization is the compatibility hook retained by the 1.20.1 API.
    }

    static {
        Registry.register(TRAVEL_PREDICATE_TYPE, Constant.id("default"), DefaultTravelPredicateType.INSTANCE);
        Registry.register(TRAVEL_PREDICATE_TYPE, Constant.id("access_weight"), AccessWeightTravelPredicateType.INSTANCE);
        Registry.register(TRAVEL_PREDICATE_TYPE, Constant.id("constant"), ConstantTravelPredicateType.INSTANCE);
        Registry.register(TRAVEL_PREDICATE_TYPE, Constant.id("and"), AndTravelPredicateType.INSTANCE);
        Registry.register(TRAVEL_PREDICATE_TYPE, Constant.id("or"), OrTravelPredicateType.INSTANCE);

        Registry.register(ROCKET_CONE_TYPE, Constant.Misc.INVALID, InvalidRocketConeType.INSTANCE);
        Registry.register(ROCKET_BODY_TYPE, Constant.Misc.INVALID, InvalidRocketBodyType.INSTANCE);
        Registry.register(ROCKET_FIN_TYPE, Constant.Misc.INVALID, InvalidRocketFinType.INSTANCE);
        Registry.register(ROCKET_BOOSTER_TYPE, Constant.Misc.INVALID, InvalidRocketBoosterType.INSTANCE);
        Registry.register(ROCKET_BOTTOM_TYPE, Constant.Misc.INVALID, InvalidRocketBottomType.INSTANCE);
        Registry.register(ROCKET_UPGRADE_TYPE, Constant.Misc.INVALID, InvalidRocketUpgradeType.INSTANCE);

        Registry.register(ROCKET_CONE_TYPE, Constant.id("basic"), BasicRocketConeType.INSTANCE);
        Registry.register(ROCKET_BODY_TYPE, Constant.id("basic"), BasicRocketBodyType.INSTANCE);
        Registry.register(ROCKET_FIN_TYPE, Constant.id("basic"), BasicRocketFinType.INSTANCE);
        Registry.register(ROCKET_BOOSTER_TYPE, Constant.id("basic"), BasicRocketBoosterType.INSTANCE);
        Registry.register(ROCKET_BOTTOM_TYPE, Constant.id("basic"), BasicRocketBottomType.INSTANCE);

        Registry.register(ROCKET_PART_RECIPE_TYPE, Constant.id("slotted_default"), PatternedRocketPartRecipeType.INSTANCE);
        Registry.register(ROCKET_UPGRADE_TYPE, Constant.id("no_upgrade"), NoUpgradeRocketUpgradeType.INSTANCE);
    }
}
