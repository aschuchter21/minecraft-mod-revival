package dev.orbitalindustries.network;

import dev.orbitalindustries.OrbitalIndustries;
import dev.orbitalindustries.station.StationState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class SpaceNetworkSavedData extends SavedData {
    private static final String DATA_NAME = OrbitalIndustries.MOD_ID + "_space_network";

    private final Map<UUID, SpaceNetworkNode> nodes = new LinkedHashMap<>();
    private final Map<UUID, CargoRoute> routes = new LinkedHashMap<>();
    private final Map<UUID, StationState> stations = new LinkedHashMap<>();

    public static SpaceNetworkSavedData get(ServerLevel level) {
        ServerLevel storageLevel = level.getServer().overworld();
        return storageLevel.getDataStorage().computeIfAbsent(
                SpaceNetworkSavedData::load,
                SpaceNetworkSavedData::new,
                DATA_NAME
        );
    }

    public void upsertNode(SpaceNetworkNode node) {
        nodes.put(node.id(), node);
        setDirty();
    }

    public void removeNode(UUID nodeId) {
        nodes.remove(nodeId);
        stations.remove(nodeId);
        routes.values().removeIf(route -> route.originNodeId().equals(nodeId) || route.destinationNodeId().equals(nodeId));
        setDirty();
    }

    public void upsertRoute(CargoRoute route) {
        routes.put(route.id(), route);
        setDirty();
    }

    public void upsertStation(StationState station) {
        stations.put(station.nodeId(), station);
        setDirty();
    }

    public Collection<SpaceNetworkNode> nodes() {
        return nodes.values();
    }

    public Collection<CargoRoute> routes() {
        return routes.values();
    }

    public Collection<StationState> stations() {
        return stations.values();
    }

    public long count(NetworkNodeType type) {
        return nodes.values().stream().filter(node -> node.type() == type).count();
    }

    public String summary() {
        return "Network online | Stations: " + count(NetworkNodeType.STATION)
                + " | Colonies: " + count(NetworkNodeType.COLONY)
                + " | Satellites: " + count(NetworkNodeType.SATELLITE)
                + " | Depots: " + count(NetworkNodeType.ORBITAL_DEPOT)
                + " | Routes: " + routes.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag nodeList = new ListTag();
        for (SpaceNetworkNode node : nodes.values()) {
            nodeList.add(node.save());
        }
        tag.put("Nodes", nodeList);

        ListTag routeList = new ListTag();
        for (CargoRoute route : routes.values()) {
            routeList.add(route.save());
        }
        tag.put("Routes", routeList);

        ListTag stationList = new ListTag();
        for (StationState station : stations.values()) {
            stationList.add(station.save());
        }
        tag.put("Stations", stationList);
        return tag;
    }

    private static SpaceNetworkSavedData load(CompoundTag tag) {
        SpaceNetworkSavedData data = new SpaceNetworkSavedData();

        ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nodeList.size(); i++) {
            SpaceNetworkNode node = SpaceNetworkNode.load(nodeList.getCompound(i));
            data.nodes.put(node.id(), node);
        }

        ListTag routeList = tag.getList("Routes", Tag.TAG_COMPOUND);
        for (int i = 0; i < routeList.size(); i++) {
            CargoRoute route = CargoRoute.load(routeList.getCompound(i));
            data.routes.put(route.id(), route);
        }

        ListTag stationList = tag.getList("Stations", Tag.TAG_COMPOUND);
        for (int i = 0; i < stationList.size(); i++) {
            StationState station = StationState.load(stationList.getCompound(i));
            data.stations.put(station.nodeId(), station);
        }

        return data;
    }
}
