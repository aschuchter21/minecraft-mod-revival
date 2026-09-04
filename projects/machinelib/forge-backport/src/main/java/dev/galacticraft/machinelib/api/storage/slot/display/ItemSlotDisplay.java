/* Copyright (c) 2021-2023 Team Galacticraft - MIT */
package dev.galacticraft.machinelib.api.storage.slot.display;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

/** UI metadata retained from MachineLib 0.3 for source compatibility. */
public record ItemSlotDisplay(int x, int y, Pair<ResourceLocation, ResourceLocation> icon) {
    public static ItemSlotDisplay create(int x, int y, Pair<ResourceLocation, ResourceLocation> icon) {
        return new ItemSlotDisplay(x, y, icon);
    }
}
