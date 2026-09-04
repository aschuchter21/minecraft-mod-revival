/*
 * Copyright (c) 2021-2023 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */
package dev.galacticraft.machinelib.api.transfer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/** Describes which direction a machine face permits a resource to move. */
public enum ResourceFlow {
    INPUT(Component.translatable("ui.machinelib.resource.flow.in").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))),
    OUTPUT(Component.translatable("ui.machinelib.resource.flow.out").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED))),
    BOTH(Component.translatable("ui.machinelib.resource.flow.both").setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)));

    public static final ResourceFlow[] VALUES = values();
    private final Component name;

    ResourceFlow(Component name) {
        this.name = name;
    }

    public static ResourceFlow getFromOrdinal(byte ordinal) {
        return VALUES[ordinal];
    }

    public Component getName() {
        return this.name;
    }

    public boolean canFlowIn(ResourceFlow flow) {
        return this == flow || this == BOTH || flow == BOTH;
    }
}
