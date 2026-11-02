/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.machine.configuration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Loader-neutral MachineLib 0.3 security settings.
 * Team membership resolution is still a later integration slice; public/private
 * ownership semantics and the original client-facing API are preserved here.
 */
public final class SecuritySettings {
    private UUID owner;
    private String username;
    private ResourceLocation team;
    private String teamName;
    private AccessLevel accessLevel = AccessLevel.PUBLIC;

    private SecuritySettings() {}

    public static SecuritySettings create() { return new SecuritySettings(); }

    public boolean isOwner(Player player) { return player != null && isOwner(player.getUUID()); }
    public boolean isOwner(@Nullable UUID uuid) { return this.owner != null && this.owner.equals(uuid); }

    public boolean hasAccess(Player player) {
        return player != null && hasAccess(player.getUUID());
    }

    public boolean hasAccess(UUID uuid) {
        if (this.owner == null || this.accessLevel == AccessLevel.PUBLIC) return true;
        if (isOwner(uuid)) return true;
        // Full team lookup is restored when Forge team/security networking lands.
        return this.accessLevel == AccessLevel.TEAM && this.team == null;
    }

    public boolean hasOwner() { return this.owner != null; }
    public @Nullable UUID getOwner() { return this.owner; }
    public @Nullable String getUsername() { return this.username; }
    public void setUsername(@Nullable String username) { this.username = username; }

    public @Nullable String getTeamName() { return this.teamName; }
    public @Nullable ResourceLocation getTeam() { return this.team; }
    public void setTeam(@Nullable ResourceLocation team, @Nullable String name) {
        this.team = team;
        this.teamName = name;
    }

    public AccessLevel getAccessLevel() { return this.accessLevel; }
    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel == null ? AccessLevel.PUBLIC : accessLevel;
    }

    public void setOwner(@Nullable UUID owner, String name) {
        this.owner = owner;
        this.username = name;
    }
}
