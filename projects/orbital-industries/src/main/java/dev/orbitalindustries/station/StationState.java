package dev.orbitalindustries.station;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public record StationState(UUID nodeId, String name, Map<StationModuleType, Integer> modules,
                           int crewCapacity, int powerGeneration, int powerDemand,
                           int oxygenProduction, int oxygenDemand, int dockingPorts, int cargoSlots) {
    public StationState {
        modules = Map.copyOf(modules);
    }

    public static StationState fromModules(UUID nodeId, String name, Map<StationModuleType, Integer> modules) {
        int crewCapacity = 0;
        int powerGeneration = 0;
        int powerDemand = 5;
        int oxygenProduction = 0;
        int dockingPorts = 0;
        int cargoSlots = 0;

        for (Map.Entry<StationModuleType, Integer> entry : modules.entrySet()) {
            StationModuleType type = entry.getKey();
            int count = Math.max(0, entry.getValue());
            crewCapacity += type.crewCapacity() * count;
            powerGeneration += type.powerGeneration() * count;
            powerDemand += type.powerDemand() * count;
            oxygenProduction += type.oxygenProduction() * count;
            dockingPorts += type.dockingPorts() * count;
            cargoSlots += type.cargoSlots() * count;
        }

        int oxygenDemand = crewCapacity * 2;
        return new StationState(nodeId, name, modules, crewCapacity, powerGeneration, powerDemand,
                oxygenProduction, oxygenDemand, dockingPorts, cargoSlots);
    }

    public int totalModules() {
        return modules.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int netPower() {
        return powerGeneration - powerDemand;
    }

    public int netOxygen() {
        return oxygenProduction - oxygenDemand;
    }

    public StationOperationalStatus operationalStatus() {
        if (totalModules() == 0) {
            return StationOperationalStatus.INCOMPLETE;
        }

        boolean powerDeficit = netPower() < 0;
        boolean oxygenDeficit = netOxygen() < 0;
        if (powerDeficit && oxygenDeficit) {
            return StationOperationalStatus.POWER_AND_OXYGEN_DEFICIT;
        }
        if (powerDeficit) {
            return StationOperationalStatus.POWER_DEFICIT;
        }
        if (oxygenDeficit) {
            return StationOperationalStatus.OXYGEN_DEFICIT;
        }
        return StationOperationalStatus.OPERATIONAL;
    }

    public boolean isOperational() {
        return operationalStatus().isOperational();
    }

    public String summary() {
        return name + " | Status: " + operationalStatus().name()
                + " | Modules: " + totalModules()
                + " | Crew capacity: " + crewCapacity
                + " | Power: " + signed(netPower())
                + " | Oxygen: " + signed(netOxygen())
                + " | Docking: " + dockingPorts
                + " | Cargo slots: " + cargoSlots;
    }

    private static String signed(int value) {
        return value >= 0 ? "+" + value : Integer.toString(value);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("NodeId", nodeId);
        tag.putString("Name", name);

        CompoundTag moduleTag = new CompoundTag();
        for (Map.Entry<StationModuleType, Integer> entry : modules.entrySet()) {
            moduleTag.putInt(entry.getKey().name(), entry.getValue());
        }
        tag.put("Modules", moduleTag);

        tag.putInt("CrewCapacity", crewCapacity);
        tag.putInt("PowerGeneration", powerGeneration);
        tag.putInt("PowerDemand", powerDemand);
        tag.putInt("OxygenProduction", oxygenProduction);
        tag.putInt("OxygenDemand", oxygenDemand);
        tag.putInt("DockingPorts", dockingPorts);
        tag.putInt("CargoSlots", cargoSlots);
        return tag;
    }

    public static StationState load(CompoundTag tag) {
        EnumMap<StationModuleType, Integer> modules = new EnumMap<>(StationModuleType.class);
        CompoundTag moduleTag = tag.getCompound("Modules");
        for (StationModuleType type : StationModuleType.values()) {
            if (moduleTag.contains(type.name())) {
                modules.put(type, Math.max(0, moduleTag.getInt(type.name())));
            }
        }

        return new StationState(
                tag.getUUID("NodeId"),
                tag.getString("Name"),
                modules,
                tag.getInt("CrewCapacity"),
                tag.getInt("PowerGeneration"),
                tag.getInt("PowerDemand"),
                tag.getInt("OxygenProduction"),
                tag.getInt("OxygenDemand"),
                tag.getInt("DockingPorts"),
                tag.getInt("CargoSlots")
        );
    }
}
