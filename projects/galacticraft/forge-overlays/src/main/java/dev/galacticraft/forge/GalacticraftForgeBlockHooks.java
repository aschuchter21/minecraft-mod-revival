/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.forge;

import dev.galacticraft.mod.content.GCBlocks;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;

/** Forge-native block interaction hooks replacing Fabric content registries. */
public final class GalacticraftForgeBlockHooks {
    private GalacticraftForgeBlockHooks() {
    }

    /** Preserve Fabric FlattenableBlockRegistry's Moon Dirt -> Moon Dirt Path behavior. */
    public static void onBlockToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() == ToolActions.SHOVEL_FLATTEN && event.getState().is(GCBlocks.MOON_DIRT)) {
            event.setFinalState(GCBlocks.MOON_DIRT_PATH.defaultBlockState());
        }
    }
}
