/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.transfer;

/** Controls machine-slot interaction for automation and players. */
public enum InputType {
    INPUT(0x009001, true, false, true),
    OUTPUT(0xa7071e, false, true, false),
    RECIPE_OUTPUT(0xa7071e, false, true, false),
    STORAGE(0x008d90, true, true, true),
    TRANSFER(0x908400, false, false, true);

    private final int colour;
    private final boolean externalInsert;
    private final boolean externalExtract;
    private final boolean playerInsert;

    InputType(int colour, boolean externalInsert, boolean externalExtract, boolean playerInsert) {
        this.colour = colour;
        this.externalInsert = externalInsert;
        this.externalExtract = externalExtract;
        this.playerInsert = playerInsert;
    }

    public boolean externalExtraction() { return this.externalExtract; }
    public boolean externalInsertion() { return this.externalInsert; }
    public ResourceFlow getExternalFlow() {
        return this.externalExtract ? (this.externalInsert ? ResourceFlow.BOTH : ResourceFlow.OUTPUT)
                : (this.externalInsert ? ResourceFlow.INPUT : null);
    }
    public boolean playerInsertion() { return this.playerInsert; }
    public boolean playerExtraction() { return true; }
    public boolean isInput() { return this == INPUT; }
    public boolean isOutput() { return this == OUTPUT || this == RECIPE_OUTPUT; }
    public int colour() { return this.colour; }
}
