package dev.orbitalindustries.content.block;

import dev.orbitalindustries.station.StationModuleType;
import net.minecraft.world.level.block.Block;

public final class StationModuleBlock extends Block {
    private final StationModuleType moduleType;

    public StationModuleBlock(StationModuleType moduleType, Properties properties) {
        super(properties);
        this.moduleType = moduleType;
    }

    public StationModuleType getModuleType() {
        return moduleType;
    }
}
