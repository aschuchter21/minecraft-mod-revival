/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.impl.machine.MachineStatusImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/** MachineLib 0.2-compatible status object used by Galacticraft 1.20.1. */
public interface MachineStatus {
    MachineStatus INVALID = create(Component.translatable("status.machinelib.invalid"), Type.OTHER);

    static MachineStatus create(Component name, Type type) {
        return new MachineStatusImpl(name, type);
    }

    static MachineStatus create(String key, ChatFormatting color, Type type) {
        return create(Component.translatable(key).setStyle(Style.EMPTY.withColor(color)), type);
    }

    /**
     * Registry serialization is part of the later networking checkpoint. Keeping
     * this factory source-compatible lets Galacticraft declare its status constants
     * now without Fabric registry calls.
     */
    static MachineStatus createAndRegister(ResourceLocation id, Component name, Type type) {
        return create(name, type);
    }

    Component name();
    Type type();

    default Component getText() { return name(); }
    default Type getType() { return type(); }

    enum Type {
        WORKING(true), PARTIALLY_WORKING(true), MISSING_RESOURCE(false), MISSING_FLUIDS(false),
        MISSING_ENERGY(false), MISSING_ITEMS(false), OUTPUT_FULL(false), OTHER(false);

        private final boolean active;
        Type(boolean active) { this.active = active; }
        public boolean isActive() { return this.active; }
    }
}
