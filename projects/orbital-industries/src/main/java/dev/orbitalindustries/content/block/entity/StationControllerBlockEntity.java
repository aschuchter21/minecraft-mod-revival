package dev.orbitalindustries.content.block.entity;

import dev.orbitalindustries.content.block.StationModuleBlock;
import dev.orbitalindustries.network.NetworkNodeType;
import dev.orbitalindustries.network.SpaceNetworkNode;
import dev.orbitalindustries.network.SpaceNetworkSavedData;
import dev.orbitalindustries.registry.OIBlockEntities;
import dev.orbitalindustries.station.StationModuleType;
import dev.orbitalindustries.station.StationState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class StationControllerBlockEntity extends BlockEntity {
    private static final int MAX_MODULES = 512;
    private static final int MAX_DISTANCE = 32;

    private UUID nodeId = UUID.randomUUID();

    public StationControllerBlockEntity(BlockPos pos, BlockState state) {
        super(OIBlockEntities.STATION_CONTROLLER.get(), pos, state);
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public String scanAndRegister(ServerLevel level) {
        Map<StationModuleType, Integer> modules = discoverModules(level);
        StationState station = StationState.fromModules(nodeId, "Orbital Station", modules);

        SpaceNetworkSavedData network = SpaceNetworkSavedData.get(level);
        network.upsertNode(new SpaceNetworkNode(
                nodeId,
                NetworkNodeType.STATION,
                station.name(),
                level.dimension().location(),
                worldPosition,
                1
        ));
        network.upsertStation(station);
        return station.summary();
    }

    private Map<StationModuleType, Integer> discoverModules(ServerLevel level) {
        EnumMap<StationModuleType, Integer> counts = new EnumMap<>(StationModuleType.class);
        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        for (Direction direction : Direction.values()) {
            open.add(worldPosition.relative(direction));
        }

        int modulesFound = 0;
        while (!open.isEmpty() && modulesFound < MAX_MODULES) {
            BlockPos current = open.removeFirst();
            if (!visited.add(current) || !withinScanBounds(current)) {
                continue;
            }

            BlockState state = level.getBlockState(current);
            if (!(state.getBlock() instanceof StationModuleBlock moduleBlock)) {
                continue;
            }

            counts.merge(moduleBlock.getModuleType(), 1, Integer::sum);
            modulesFound++;

            for (Direction direction : Direction.values()) {
                open.add(current.relative(direction));
            }
        }

        return counts;
    }

    private boolean withinScanBounds(BlockPos pos) {
        return Math.abs(pos.getX() - worldPosition.getX()) <= MAX_DISTANCE
                && Math.abs(pos.getY() - worldPosition.getY()) <= MAX_DISTANCE
                && Math.abs(pos.getZ() - worldPosition.getZ()) <= MAX_DISTANCE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putUUID("NetworkNodeId", nodeId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("NetworkNodeId")) {
            nodeId = tag.getUUID("NetworkNodeId");
        }
    }
}
