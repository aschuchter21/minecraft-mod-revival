/*
 * Temporary Forge migration ABI bridge for Galacticraft 1.20.1.
 *
 * This class intentionally keeps the Fabric API binary name used by the recovered
 * Galacticraft bytecode while applying vanilla flammability metadata. It is not a
 * Fabric dependency and can be removed once GCBlocks itself is source-ported.
 */
package net.fabricmc.fabric.api.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@Deprecated(forRemoval = true)
public final class FlammableBlockRegistry {
    private static final FlammableBlockRegistry DEFAULT = new FlammableBlockRegistry();
    private static final Method SET_FLAMMABLE = findSetFlammable();

    private FlammableBlockRegistry() {
    }

    public static FlammableBlockRegistry getDefaultInstance() {
        return DEFAULT;
    }

    public void add(Block block, int burnChance, int spreadChance) {
        try {
            SET_FLAMMABLE.invoke((FireBlock) Blocks.FIRE, block, burnChance, spreadChance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to register Galacticraft block flammability", e);
        }
    }

    private static Method findSetFlammable() {
        Method method = Arrays.stream(FireBlock.class.getDeclaredMethods())
                .filter(candidate -> candidate.getReturnType() == void.class)
                .filter(candidate -> Arrays.equals(candidate.getParameterTypes(),
                        new Class<?>[]{Block.class, int.class, int.class}))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not locate FireBlock flammability registration method"));
        method.setAccessible(true);
        return method;
    }
}
