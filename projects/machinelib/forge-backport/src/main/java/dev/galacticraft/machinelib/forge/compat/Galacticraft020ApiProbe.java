/* Compile-only compatibility probe for Galacticraft 5.0.0-prealpha / MachineLib 0.2.0+f0eaafd6. */
package dev.galacticraft.machinelib.forge.compat;

import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Items;

/** Not used at runtime; its purpose is to make CI compile Galacticraft's 0.2-style builder surface. */
final class Galacticraft020ApiProbe {
    private static final SlotGroupType ENERGY_TO_ITEM = SlotGroupType.create(
            TextColor.fromRgb(0xb5b41c), Component.literal("energy drain"), InputType.TRANSFER);
    private static final SlotGroupType COAL = SlotGroupType.create(
            TextColor.fromRgb(0), Component.literal("coal"), InputType.INPUT);

    static MachineEnergyStorage energy() {
        return MachineEnergyStorage.of(100_000, 240, 240, false, true);
    }

    static MachineItemStorage items() {
        return MachineItemStorage.builder()
                .single(ENERGY_TO_ITEM, ItemResourceSlot.builder()
                        .pos(8, 62)
                        .filter(ResourceFilters.CAN_INSERT_ENERGY)
                        .strictFilter(ResourceFilters.CAN_INSERT_ENERGY_STRICT)
                        ::build)
                .single(COAL, ItemResourceSlot.builder()
                        .pos(71, 53)
                        .filter((item, tag) -> item == Items.COAL || item == Items.CHARCOAL || item == Items.COAL_BLOCK)
                        ::build)
                .build();
    }

    static ItemResourceSlot coalSlot() {
        return items().getGroup(COAL).getSlot(0);
    }
}
