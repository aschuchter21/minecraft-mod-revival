package dev.orbitalindustries.menu;

import dev.orbitalindustries.content.block.entity.MissionControlBlockEntity;
import dev.orbitalindustries.network.NetworkNodeType;
import dev.orbitalindustries.network.SpaceNetworkSavedData;
import dev.orbitalindustries.registry.OIBlocks;
import dev.orbitalindustries.registry.OIMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class MissionControlMenu extends AbstractContainerMenu {
    private static final int STATIONS = 0;
    private static final int COLONIES = 1;
    private static final int SATELLITES = 2;
    private static final int DEPOTS = 3;
    private static final int ROUTES = 4;
    private static final int DATA_COUNT = 5;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    public MissionControlMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, pos, new SimpleContainerData(DATA_COUNT));
    }

    public MissionControlMenu(int containerId, Inventory inventory, MissionControlBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity.getBlockPos(), createServerData(blockEntity));
    }

    private MissionControlMenu(int containerId, Inventory inventory, BlockPos pos, ContainerData data) {
        super(OIMenus.MISSION_CONTROL.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.data = data;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);
        addDataSlots(data);
    }

    private static ContainerData createServerData(MissionControlBlockEntity blockEntity) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (!(blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
                    return 0;
                }
                SpaceNetworkSavedData network = SpaceNetworkSavedData.get(serverLevel);
                return switch (index) {
                    case STATIONS -> (int) network.count(NetworkNodeType.STATION);
                    case COLONIES -> (int) network.count(NetworkNodeType.COLONY);
                    case SATELLITES -> (int) network.count(NetworkNodeType.SATELLITE);
                    case DEPOTS -> (int) network.count(NetworkNodeType.ORBITAL_DEPOT);
                    case ROUTES -> network.routes().size();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    public int stationCount() {
        return data.get(STATIONS);
    }

    public int colonyCount() {
        return data.get(COLONIES);
    }

    public int satelliteCount() {
        return data.get(SATELLITES);
    }

    public int depotCount() {
        return data.get(DEPOTS);
    }

    public int routeCount() {
        return data.get(ROUTES);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, OIBlocks.MISSION_CONTROL_CONSOLE.get());
    }
}
