package dev.orbitalindustries.station;

public enum StationOperationalStatus {
    INCOMPLETE,
    POWER_AND_OXYGEN_DEFICIT,
    POWER_DEFICIT,
    OXYGEN_DEFICIT,
    OPERATIONAL;

    public boolean isOperational() {
        return this == OPERATIONAL;
    }
}
