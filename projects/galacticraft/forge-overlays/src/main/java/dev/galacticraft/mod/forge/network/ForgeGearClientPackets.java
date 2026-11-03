/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.network;

import dev.galacticraft.api.accessor.GearInventoryProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only gear sync receiver. Never reference this from common class
 * initialization; ForgeGearNetworking invokes it through DistExecutor.
 */
public final class ForgeGearClientPackets {
    private ForgeGearClientPackets() {
    }

    public static void apply(ForgeGearNetworking.SyncGearPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        Entity entity = minecraft.level.getEntity(packet.entityId());
        if (!(entity instanceof GearInventoryProvider provider)) return;

        Container inventory = provider.getGearInv();
        int slots = Math.min(inventory.getContainerSize(), packet.stacks().size());
        for (int i = 0; i < slots; i++) {
            ItemStack copy = packet.stacks().get(i).copy();
            inventory.setItem(i, copy);
        }
    }
}
