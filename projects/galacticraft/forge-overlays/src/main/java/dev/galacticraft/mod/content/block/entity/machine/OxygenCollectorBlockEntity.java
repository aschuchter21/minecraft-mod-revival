/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.machine;

import dev.galacticraft.api.gas.Gases;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.api.universe.celestialbody.CelestialBodyConfig;
import dev.galacticraft.api.universe.celestialbody.landable.Landable;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.mod.Galacticraft;
import dev.galacticraft.mod.content.GCMachineTypes;
import dev.galacticraft.mod.machine.GCMachineStatuses;
import dev.galacticraft.mod.screen.OxygenCollectorMenu;
import dev.galacticraft.mod.util.FluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 overlay for Galacticraft's oxygen collector. */
public class OxygenCollectorBlockEntity extends MachineBlockEntity {
    public static final int CHARGE_SLOT = 0;
    public static final int OXYGEN_TANK = 0;
    public static final long MAX_OXYGEN = FluidUtil.bucketsToDroplets(50);

    public int collectionAmount;
    private boolean oxygenWorld;

    public OxygenCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(GCMachineTypes.OXYGEN_COLLECTOR, pos, state);
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        CelestialBody<CelestialBodyConfig, ? extends Landable<CelestialBodyConfig>> body =
                CelestialBody.getByDimension(world).orElse(null);
        this.oxygenWorld = body == null || body.atmosphere().breathable();
    }

    private int collectOxygen(@NotNull ServerLevel world, @NotNull BlockPos pos) {
        if (!this.oxygenWorld) {
            int minX = pos.getX() - 5;
            int minY = pos.getY() - 5;
            int minZ = pos.getZ() - 5;
            int maxX = pos.getX() + 5;
            int maxY = pos.getY() + 5;
            int maxZ = pos.getZ() + 5;

            float leafBlocks = 0;
            for (BlockPos scanPos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
                BlockState scanState = world.getBlockState(scanPos);
                if (scanState.isAir()) continue;
                if (scanState.getBlock() instanceof LeavesBlock && !scanState.getValue(LeavesBlock.PERSISTENT)) {
                    leafBlocks++;
                } else if (scanState.getBlock() instanceof CropBlock) {
                    leafBlocks += 0.75F;
                }
            }

            if (leafBlocks < 2) return 0;
            double oxyCount = 20 * (leafBlocks / 14.0F);
            return (int) Math.ceil(oxyCount) / 20;
        }
        return 183 / 20;
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        this.chargeFromStack(CHARGE_SLOT);
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        profiler.push("transfer");
        this.trySpreadFluids(level, state);

        if (this.fluidStorage().getSlot(OXYGEN_TANK).isFull()) {
            profiler.pop();
            return GCMachineStatuses.OXYGEN_TANK_FULL;
        }

        profiler.popPush("collect");
        try {
            long energy = Galacticraft.CONFIG_MANAGER.get().oxygenCollectorEnergyConsumptionRate();
            if (!this.energyStorage().canExtract(energy)) {
                this.collectionAmount = 0;
                return MachineStatuses.NOT_ENOUGH_ENERGY;
            }

            this.collectionAmount = collectOxygen(level, pos);
            if (this.collectionAmount <= 0) return GCMachineStatuses.NOT_ENOUGH_OXYGEN;

            this.energyStorage().extract(energy);
            this.fluidStorage().getSlot(OXYGEN_TANK)
                    .insert(Gases.OXYGEN, FluidUtil.bucketsToDroplets(this.collectionAmount));
            return GCMachineStatuses.COLLECTING;
        } finally {
            profiler.pop();
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        if (this.getSecurity().hasAccess(player)) {
            return new OxygenCollectorMenu(syncId, (ServerPlayer) player, this);
        }
        return null;
    }

    public int getCollectionAmount() {
        return this.collectionAmount;
    }
}
