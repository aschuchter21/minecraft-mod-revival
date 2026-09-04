/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine.configuration;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Minimal server-side MachineLib 0.2 security model. Machines are public until
 * an owner is assigned; owner assignment is retained for later UI/network work.
 */
public final class SecuritySettings {
    private UUID owner;
    private String username;

    private SecuritySettings() {}

    public static SecuritySettings create() { return new SecuritySettings(); }

    public boolean isOwner(Player player) { return player != null && isOwner(player.getUUID()); }
    public boolean isOwner(UUID uuid) { return this.owner != null && this.owner.equals(uuid); }
    public boolean hasAccess(Player player) { return this.owner == null || isOwner(player); }
    public boolean hasAccess(UUID uuid) { return this.owner == null || isOwner(uuid); }
    public boolean hasOwner() { return this.owner != null; }
    public UUID getOwner() { return this.owner; }
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    public void setOwner(UUID owner, String name) { this.owner = owner; this.username = name; }
}
