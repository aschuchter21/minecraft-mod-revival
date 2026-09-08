/*
 * Temporary Forge migration ABI bridge for Galacticraft 1.20.1.
 *
 * This class intentionally keeps the Fabric API binary name used by the recovered
 * Galacticraft bytecode while delegating flammability registration to vanilla.
 * The Forge access transformer widens FireBlock#setFlammable for this bridge.
 */
package net.fabricmc.fabric.api.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

@Deprecated(forRemoval = true)
public final class FlammableBlockRegistry {
    private static final FlammableBlockRegistry DEFAULT = new FlammableBlockRegistry();

    private FlammableBlockRegistry() {
    }

    public static FlammableBlockRegistry getDefaultInstance() {
        return DEFAULT;
    }

    public void add(Block block, int burnChance, int spreadChance) {
        ((FireBlock) Blocks.FIRE).setFlammable(block, burnChance, spreadChance);
    }
}
