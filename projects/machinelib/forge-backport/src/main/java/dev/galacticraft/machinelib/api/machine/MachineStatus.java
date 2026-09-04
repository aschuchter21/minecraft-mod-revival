/*
 * Copyright (c) 2021-2026 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.impl.machine.MachineStatusImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/** Binary-compatible server-side subset of MachineLib 0.3's machine status API. */
public interface MachineStatus {
    static MachineStatus create(Component name, Type type) {
        return new MachineStatusImpl(name, type);
    }

    static MachineStatus create(String key, ChatFormatting color, Type type) {
        return create(Component.translatable(key).setStyle(Style.EMPTY.withColor(color)), type);
    }

    Component getText();
    Type getType();

    default void writePacket(MachineType<?, ?> type, FriendlyByteBuf buf) {
        buf.writeByte(type.statusDomain().indexOf(this));
    }

    static MachineStatus readPacket(MachineType<?, ?> type, FriendlyByteBuf buf) {
        return type.statusDomain().get(buf.readByte());
    }

    enum Type {
        WORKING(true), PARTIALLY_WORKING(true), MISSING_RESOURCE(false), MISSING_FLUIDS(false),
        MISSING_ENERGY(false), MISSING_ITEMS(false), OUTPUT_FULL(false), OTHER(false);

        private final boolean active;
        Type(boolean active) { this.active = active; }
        public boolean isActive() { return this.active; }
    }
}
