package dev.orbitalindustries.station;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record StationModuleClaim(ResourceLocation dimension, BlockPos pos, UUID stationNodeId) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dimension", dimension.toString());
        tag.putLong("Pos", pos.asLong());
        tag.putUUID("StationNodeId", stationNodeId);
        return tag;
    }

    public static StationModuleClaim load(CompoundTag tag) {
        return new StationModuleClaim(
                new ResourceLocation(tag.getString("Dimension")),
                BlockPos.of(tag.getLong("Pos")),
                tag.getUUID("StationNodeId")
        );
    }

    public String key() {
        return key(dimension, pos);
    }

    public static String key(ResourceLocation dimension, BlockPos pos) {
        return dimension + "|" + pos.asLong();
    }
}
