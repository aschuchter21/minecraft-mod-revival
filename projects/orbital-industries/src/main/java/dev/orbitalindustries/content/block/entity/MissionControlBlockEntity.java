package dev.orbitalindustries.content.block.entity;

import dev.orbitalindustries.menu.MissionControlMenu;
import dev.orbitalindustries.network.NetworkNodeType;
import dev.orbitalindustries.network.SpaceNetworkNode;
import dev.orbitalindustries.network.SpaceNetworkSavedData;
import dev.orbitalindustries.registry.OIBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class MissionControlBlockEntity extends BlockEntity implements MenuProvider {
    private UUID nodeId = UUID.randomUUID();

    public MissionControlBlockEntity(BlockPos pos, BlockState state) {
        super(OIBlockEntities.MISSION_CONTROL.get(), pos, state);
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public String ensureRegistered(ServerLevel level) {
        SpaceNetworkSavedData network = SpaceNetworkSavedData.get(level);
        network.upsertNode(new SpaceNetworkNode(
                nodeId,
                NetworkNodeType.MISSION_CONTROL,
                "Mission Control",
                level.dimension().location(),
                worldPosition,
                1
        ));
        return network.summary();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.orbitalindustries.mission_control.title");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MissionControlMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putUUID("NetworkNodeId", nodeId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("NetworkNodeId")) {
            nodeId = tag.getUUID("NetworkNodeId");
        }
    }
}
