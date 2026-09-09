package dev.orbitalindustries.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SpaceNetworkNode(UUID id, NetworkNodeType type, String name,
                               ResourceLocation dimension, BlockPos anchor, int tier) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putString("Type", type.name());
        tag.putString("Name", name);
        tag.putString("Dimension", dimension.toString());
        tag.putLong("Anchor", anchor.asLong());
        tag.putInt("Tier", tier);
        return tag;
    }

    public static SpaceNetworkNode load(CompoundTag tag) {
        UUID id = tag.hasUUID("Id") ? tag.getUUID("Id") : UUID.randomUUID();
        NetworkNodeType type;
        try {
            type = NetworkNodeType.valueOf(tag.getString("Type"));
        } catch (IllegalArgumentException ignored) {
            type = NetworkNodeType.MISSION_CONTROL;
        }
        String name = tag.getString("Name");
        ResourceLocation dimension = new ResourceLocation(tag.getString("Dimension"));
        BlockPos anchor = BlockPos.of(tag.getLong("Anchor"));
        int tier = Math.max(0, tag.getInt("Tier"));
        return new SpaceNetworkNode(id, type, name, dimension, anchor, tier);
    }
}
