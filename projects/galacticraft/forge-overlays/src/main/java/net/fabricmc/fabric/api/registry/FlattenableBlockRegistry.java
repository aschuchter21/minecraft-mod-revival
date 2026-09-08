/*
 * Temporary Forge migration ABI bridge for Galacticraft 1.20.1.
 *
 * The recovered GCBlocks bytecode calls Fabric's flattenable registry. Forge
 * exposes the same behavior through BlockToolModificationEvent, so keep the
 * binary entrypoint and route it to the Forge event bus until GCBlocks is fully
 * source-ported.
 */
package net.fabricmc.fabric.api.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;

import java.util.IdentityHashMap;
import java.util.Map;

@Deprecated(forRemoval = true)
public final class FlattenableBlockRegistry {
    private static final Map<Block, BlockState> FLATTENABLES = new IdentityHashMap<>();
    private static boolean listenerRegistered;

    private FlattenableBlockRegistry() {
    }

    public static synchronized void register(Block block, BlockState flattenedState) {
        FLATTENABLES.put(block, flattenedState);
        if (!listenerRegistered) {
            MinecraftForge.EVENT_BUS.addListener(FlattenableBlockRegistry::onToolModification);
            listenerRegistered = true;
        }
    }

    private static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() != ToolActions.SHOVEL_FLATTEN) {
            return;
        }

        BlockState flattened = FLATTENABLES.get(event.getState().getBlock());
        if (flattened != null) {
            event.setFinalState(flattened);
        }
    }
}
