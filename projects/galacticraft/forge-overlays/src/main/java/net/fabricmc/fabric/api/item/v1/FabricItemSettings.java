/*
 * Temporary Forge migration ABI bridge for Galacticraft 1.20.1.
 *
 * Only three recovered GCItems declarations instantiate FabricItemSettings; the
 * class is simply an Item.Properties subtype for those call sites.
 */
package net.fabricmc.fabric.api.item.v1;

import net.minecraft.world.item.Item;

@Deprecated(forRemoval = true)
public class FabricItemSettings extends Item.Properties {
    public FabricItemSettings() {
        super();
    }
}
