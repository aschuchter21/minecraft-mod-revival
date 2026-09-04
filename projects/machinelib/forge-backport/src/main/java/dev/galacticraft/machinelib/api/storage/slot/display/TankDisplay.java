/* Copyright (c) 2021-2023 Team Galacticraft - MIT */
package dev.galacticraft.machinelib.api.storage.slot.display;

/** UI metadata retained from MachineLib 0.3 for source compatibility. */
public record TankDisplay(int x, int y, int width, int height) {
    public static TankDisplay create(int x, int y, int width, int height) {
        return new TankDisplay(x, y, width, height);
    }
}
