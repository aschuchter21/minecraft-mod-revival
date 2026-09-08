/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.api.entity.attribute;

import dev.galacticraft.mod.Constant;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.RegisterEvent;

/** Forge lifecycle-backed Galacticraft API attributes. */
public final class GcApiEntityAttributes {
    public static final Attribute CAN_BREATHE_IN_SPACE =
            new RangedAttribute(
                    "galacticraft.attribute.name.generic.can_breathe_in_space",
                    0.0D, 0.0D, 1.0D
            ).setSyncable(true);

    public static final Attribute LOCAL_GRAVITY_LEVEL =
            new RangedAttribute(
                    "galacticraft.attribute.name.generic.local_gravity_level",
                    0.0D, 0.0D, 1.0D
            ).setSyncable(true);

    private GcApiEntityAttributes() {
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.ATTRIBUTE, Constant.id("can_breathe_in_space"),
                () -> CAN_BREATHE_IN_SPACE);
        event.register(Registries.ATTRIBUTE, Constant.id("local_gravity_level"),
                () -> LOCAL_GRAVITY_LEVEL);
    }

    /** Original initializer hook retained; Forge registration is event-driven. */
    public static void init() {
    }
}
