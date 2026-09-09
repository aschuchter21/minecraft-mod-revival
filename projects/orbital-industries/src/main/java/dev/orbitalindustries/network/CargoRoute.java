package dev.orbitalindustries.network;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record CargoRoute(UUID id, String name, UUID originNodeId, UUID destinationNodeId,
                         boolean enabled, int priority) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Name", name);
        tag.putUUID("Origin", originNodeId);
        tag.putUUID("Destination", destinationNodeId);
        tag.putBoolean("Enabled", enabled);
        tag.putInt("Priority", priority);
        return tag;
    }

    public static CargoRoute load(CompoundTag tag) {
        return new CargoRoute(
                tag.hasUUID("Id") ? tag.getUUID("Id") : UUID.randomUUID(),
                tag.getString("Name"),
                tag.getUUID("Origin"),
                tag.getUUID("Destination"),
                tag.getBoolean("Enabled"),
                tag.getInt("Priority")
        );
    }
}
