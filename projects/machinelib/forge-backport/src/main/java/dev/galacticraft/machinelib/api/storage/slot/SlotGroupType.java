/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

/**
 * MachineLib 0.2-compatible slot-group descriptor.
 * Custom registry serialization will be restored with the networking layer; for
 * the server/runtime port the descriptor identity is sufficient and matches how
 * Galacticraft declares GCSlotGroupTypes.
 */
public final class SlotGroupType {
    private final TextColor color;
    private final Component name;
    private final InputType inputType;
    private final ResourceLocation id;

    private SlotGroupType(ResourceLocation id, TextColor color, Component name, InputType inputType) {
        this.id = id;
        this.color = color;
        this.name = name;
        this.inputType = inputType;
    }

    public static SlotGroupType create(TextColor color, MutableComponent name, InputType inputType) {
        return new SlotGroupType(null, color, name.setStyle(Style.EMPTY.withColor(color)), inputType);
    }

    public static SlotGroupType createAndRegister(ResourceLocation id, TextColor color, InputType inputType) {
        return new SlotGroupType(id, color,
                Component.translatable(id.getNamespace() + ".slot_group." + id.getPath()).setStyle(Style.EMPTY.withColor(color)),
                inputType);
    }

    public TextColor color() { return this.color; }
    public Component name() { return this.name; }
    public InputType inputType() { return this.inputType; }
    public ResourceLocation id() { return this.id; }
}