package dev.orbitalindustries.station;

public enum StationModuleType {
    HABITATION(4, 10, 0, 0, 0, 0),
    LIFE_SUPPORT(0, 30, 0, 120, 0, 0),
    DOCKING_PORT(0, 8, 0, 0, 1, 0),
    SOLAR_ARRAY(0, 0, 120, 0, 0, 0),
    CARGO_STORAGE(0, 4, 0, 0, 0, 54);

    private final int crewCapacity;
    private final int powerDemand;
    private final int powerGeneration;
    private final int oxygenProduction;
    private final int dockingPorts;
    private final int cargoSlots;

    StationModuleType(int crewCapacity, int powerDemand, int powerGeneration,
                      int oxygenProduction, int dockingPorts, int cargoSlots) {
        this.crewCapacity = crewCapacity;
        this.powerDemand = powerDemand;
        this.powerGeneration = powerGeneration;
        this.oxygenProduction = oxygenProduction;
        this.dockingPorts = dockingPorts;
        this.cargoSlots = cargoSlots;
    }

    public int crewCapacity() {
        return crewCapacity;
    }

    public int powerDemand() {
        return powerDemand;
    }

    public int powerGeneration() {
        return powerGeneration;
    }

    public int oxygenProduction() {
        return oxygenProduction;
    }

    public int dockingPorts() {
        return dockingPorts;
    }

    public int cargoSlots() {
        return cargoSlots;
    }
}
