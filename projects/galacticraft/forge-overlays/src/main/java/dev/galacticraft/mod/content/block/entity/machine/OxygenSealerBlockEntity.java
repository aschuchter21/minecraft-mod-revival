/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.machine;

import dev.galacticraft.api.accessor.LevelOxygenAccessor;
import dev.galacticraft.api.gas.Gases;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.Galacticraft;
import dev.galacticraft.mod.accessor.ServerLevelAccessor;
import dev.galacticraft.mod.content.GCMachineTypes;
import dev.galacticraft.mod.machine.GCMachineStatuses;
import dev.galacticraft.mod.util.FluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/** Forge 1.20.1 overlay for Galacticraft's oxygen sealer. */
public class OxygenSealerBlockEntity extends MachineBlockEntity {
    public static final int CHARGE_SLOT = 0;
    public static final int OXYGEN_INPUT_SLOT = 1;
    public static final int OXYGEN_TANK = 0;
    public static final long MAX_OXYGEN = FluidUtil.bucketsToDroplets(50);
    public static final int SEAL_CHECK_TIME = 20;

    private final Set<BlockPos> breathablePositions = new HashSet<>();
    private final Set<BlockPos> watching = new HashSet<>();
    private int sealCheckTime;
    private boolean updateQueued = true;
    private boolean disabled;
    private boolean oxygenWorld;

    public OxygenSealerBlockEntity(BlockPos pos, BlockState state) {
        super(GCMachineTypes.OXYGEN_SEALER, pos, state);
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        this.sealCheckTime = SEAL_CHECK_TIME;
        this.oxygenWorld = CelestialBody.getByDimension(world)
                .map(body -> body.atmosphere().breathable())
                .orElse(true);
        if (!world.isClientSide && world instanceof ServerLevelAccessor accessor) {
            accessor.addSealer(this);
        }
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        this.chargeFromStack(CHARGE_SLOT);
        this.takeOxygenFromStack();
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        if (this.disabled) {
            this.disabled = false;
            if (level instanceof ServerLevelAccessor accessor) accessor.addSealer(this);
        }

        long energy = Galacticraft.CONFIG_MANAGER.get().oxygenCompressorEnergyConsumptionRate();
        if (!this.energyStorage().canExtract(energy)) {
            this.sealCheckTime = 0;
            return MachineStatuses.NOT_ENOUGH_ENERGY;
        }

        FluidResourceSlot oxygenTank = this.fluidStorage().getSlot(OXYGEN_TANK);
        if (oxygenTank.isEmpty()) {
            this.sealCheckTime = 0;
            return GCMachineStatuses.NOT_ENOUGH_OXYGEN;
        }

        if (this.sealCheckTime > 0) this.sealCheckTime--;
        if (this.updateQueued && this.sealCheckTime == 0) {
            profiler.push("check_seal");
            try {
                this.updateQueued = false;
                this.sealCheckTime = SEAL_CHECK_TIME;
                BlockPos start = pos.relative(Direction.UP);
                LevelOxygenAccessor oxygen = level instanceof LevelOxygenAccessor accessor ? accessor : null;

                if (this.oxygenWorld || (this.breathablePositions.isEmpty()
                        && oxygen != null && oxygen.isBreathable(start))) {
                    return GCMachineStatuses.ALREADY_SEALED;
                }

                this.setBreathable(level, this.breathablePositions, false);
                this.breathablePositions.clear();
                this.watching.clear();

                Queue<Tuple<BlockPos, Direction>> queue = new LinkedList<>();
                Set<Tuple<BlockPos, Direction>> checked = new HashSet<>();
                Set<BlockPos> added = new HashSet<>();
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                Tuple<BlockPos, Direction> first = new Tuple<>(start, Direction.UP);
                queue.add(first);
                checked.add(first);

                while (!queue.isEmpty()) {
                    Tuple<BlockPos, Direction> pair = queue.poll();
                    BlockPos current = pair.getA();
                    BlockState currentState = level.getBlockState(current);
                    if (currentState.isAir()
                            || !Block.isFaceFull(currentState.getCollisionShape(level, current), pair.getB().getOpposite())) {
                        this.breathablePositions.add(current);
                        if (this.breathablePositions.size() > 1000) {
                            this.breathablePositions.clear();
                            this.watching.clear();
                            this.updateQueued = true;
                            this.sealCheckTime = SEAL_CHECK_TIME * 5;
                            return GCMachineStatuses.AREA_TOO_LARGE;
                        }

                        added.add(current);
                        queue.removeIf(candidate -> candidate.getA().equals(current));
                        for (Direction direction : Constant.Misc.DIRECTIONS) {
                            Tuple<BlockPos, Direction> next = new Tuple<>(
                                    mutable.set(current).move(direction).immutable(), direction);
                            if (!added.contains(next.getA()) && checked.add(next)) {
                                if (!Block.isFaceFull(currentState.getCollisionShape(level, current), direction)) {
                                    queue.add(next);
                                }
                            }
                        }
                    } else {
                        this.watching.add(current);
                    }
                }

                this.setBreathable(level, this.breathablePositions, true);
            } finally {
                profiler.pop();
            }
        }

        profiler.push("extract");
        try {
            this.energyStorage().extract(energy);
            oxygenTank.extract(this.breathablePositions.size() * 2L);
        } finally {
            profiler.pop();
        }
        return GCMachineStatuses.SEALED;
    }

    @Override
    protected void tickDisabled(@NotNull ServerLevel world, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        this.disabled = true;
        if (world instanceof ServerLevelAccessor accessor) accessor.removeSealer(this);
        this.setBreathable(world, this.breathablePositions, false);
        this.breathablePositions.clear();
        this.watching.clear();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null) {
            if (!this.level.isClientSide && this.level instanceof ServerLevelAccessor accessor) {
                accessor.removeSealer(this);
            }
            if (this.level instanceof LevelOxygenAccessor oxygen) {
                for (BlockPos pos : this.breathablePositions) oxygen.setBreathable(pos, false);
            }
        }
        this.breathablePositions.clear();
        this.watching.clear();
    }

    /** Forge item-fluid replacement for MachineLib/Fabric FluidStorage.ITEM. */
    private void takeOxygenFromStack() {
        FluidResourceSlot target = this.fluidStorage().getSlot(OXYGEN_TANK);
        if (target.isFull()) return;

        ItemResourceSlot itemSlot = this.itemStorage().getSlot(OXYGEN_INPUT_SLOT);
        ItemStack stack = itemSlot.toStack();
        if (stack.isEmpty()) return;

        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null) return;

        long roomUnits = target.tryInsert(Gases.OXYGEN, Long.MAX_VALUE);
        int roomMb = (int) Math.min(Integer.MAX_VALUE,
                roomUnits / FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        if (roomMb <= 0) return;

        FluidStack simulated = handler.drain(new FluidStack(Gases.OXYGEN, roomMb), IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty()) return;

        FluidStack drained = handler.drain(new FluidStack(Gases.OXYGEN, simulated.getAmount()), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return;

        target.insert(Gases.OXYGEN,
                (long) drained.getAmount() * FluidResourceSlot.INTERNAL_UNITS_PER_MB);
        itemSlot.setStack(handler.getContainer());
    }

    private void setBreathable(@NotNull ServerLevel level, @NotNull Set<BlockPos> positions, boolean value) {
        if (!(level instanceof LevelOxygenAccessor oxygen)) return;
        for (BlockPos breathable : positions) oxygen.setBreathable(breathable, value);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        if (this.getSecurity().hasAccess(player)) {
            return new MachineMenu<>(syncId, (ServerPlayer) player, this);
        }
        return null;
    }

    public void enqueueUpdate(BlockPos pos, VoxelShape voxelShape) {
        if ((this.watching.contains(pos) && !Block.isShapeFullBlock(voxelShape))
                || (this.breathablePositions.contains(pos) && !voxelShape.isEmpty())) {
            this.updateQueued = true;
        }
    }
}
