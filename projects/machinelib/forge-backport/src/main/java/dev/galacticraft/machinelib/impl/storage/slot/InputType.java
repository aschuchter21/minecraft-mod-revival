/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.transfer.ResourceFlow;

/**
 * MachineLib 0.2 slot automation policy used by Galacticraft 1.20.1.
 * The Forge backport keeps this historical package/API and maps it onto the
 * loader-neutral slot engine internally.
 */
public enum InputType {
    INPUT(true, false, true),
    OUTPUT(false, true, false),
    STORAGE(true, true, true),
    TRANSFER(false, false, true);

    private final boolean externalInsert;
    private final boolean externalExtract;
    private final boolean playerInsert;

    InputType(boolean externalInsert, boolean externalExtract, boolean playerInsert) {
        this.externalInsert = externalInsert;
        this.externalExtract = externalExtract;
        this.playerInsert = playerInsert;
    }

    public boolean externalExtraction() { return this.externalExtract; }
    public boolean externalInsertion() { return this.externalInsert; }
    public boolean playerInsertion() { return this.playerInsert; }
    public boolean playerExtraction() { return true; }

    public ResourceFlow getExternalFlow() {
        if (this.externalInsert && this.externalExtract) return ResourceFlow.BOTH;
        if (this.externalInsert) return ResourceFlow.INPUT;
        if (this.externalExtract) return ResourceFlow.OUTPUT;
        return null;
    }

    public dev.galacticraft.machinelib.api.transfer.InputType toInternal() {
        return switch (this) {
            case INPUT -> dev.galacticraft.machinelib.api.transfer.InputType.INPUT;
            case OUTPUT -> dev.galacticraft.machinelib.api.transfer.InputType.OUTPUT;
            case STORAGE -> dev.galacticraft.machinelib.api.transfer.InputType.STORAGE;
            case TRANSFER -> dev.galacticraft.machinelib.api.transfer.InputType.TRANSFER;
        };
    }
}