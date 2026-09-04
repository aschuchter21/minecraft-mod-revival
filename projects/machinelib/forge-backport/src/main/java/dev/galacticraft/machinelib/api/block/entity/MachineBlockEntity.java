/*
 * Copyright (c) 2021-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.forge.capability.ForgeMachineCapabilityBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Forge 47.4.10 server/runtime port of MachineLib 0.2's base machine block entity.
 * It preserves the API Galacticraft 1.20.1 machines call while replacing Fabric
 * transfer lookups with Forge capabilities. Client rendering, configurable face
 * routing and menu packet synchronization are intentionally separate checkpoints.
 */
public abstract class MachineBlockEntity extends BlockEntity implements MenuProvider {
    private final MachineType<? extends MachineBlockEntity, ? extends AbstractContainerMenu> machineType;
    private final MachineEnergyStorage energyStorage;
    private final MachineItemStorage itemStorage;
    private final MachineFluidStorage fluidStorage;
    private final SecuritySettings security = SecuritySettings.create();
    private final Component name;
    private final ForgeMachineCapabilityBridge capabilities;

    private MachineStatus status = MachineStatus.INVALID;
    private RedstoneActivation redstoneActivation = RedstoneActivation.IGNORE;
    private boolean disableDrops;

    protected MachineBlockEntity(MachineType<? extends MachineBlockEntity, ? extends AbstractContainerMenu> type,
                                 BlockPos pos, BlockState state) {
        super(type.getBlockEntityType(), pos, state);
        this.machineType = type;
        this.name = state.getBlock().getName();
        this.energyStorage = type.createEnergyStorage();
        this.itemStorage = type.createItemStorage();
        this.fluidStorage = type.createFluidStorage();
        this.energyStorage.setListener(this::setChanged);
        this.itemStorage.setListener(this::setChanged);
        this.fluidStorage.setListener(this::setChanged);

        this.capabilities = new ForgeMachineCapabilityBridge(
                side -> this.energyStorage.getExposedStorage(ResourceFlow.BOTH),
                side -> this.itemStorage.getExposedStorage(ResourceFlow.BOTH),
                side -> this.fluidStorage.getExposedStorage(ResourceFlow.BOTH));
    }

    public MachineType<? extends MachineBlockEntity, ? extends AbstractContainerMenu> getMachineType() { return this.machineType; }
    public final MachineEnergyStorage energyStorage() { return this.energyStorage; }
    public final MachineItemStorage itemStorage() { return this.itemStorage; }
    public final MachineFluidStorage fluidStorage() { return this.fluidStorage; }
    public final SecuritySettings getSecurity() { return this.security; }

    public MachineStatus getStatus() { return this.status; }
    public void setStatus(MachineStatus status) {
        if (status == null) status = MachineStatus.INVALID;
        if (this.status != status) {
            this.status = status;
            this.setChanged();
        }
    }

    public RedstoneActivation getRedstoneActivation() { return this.redstoneActivation; }
    public void setRedstone(RedstoneActivation activation) {
        this.redstoneActivation = activation == null ? RedstoneActivation.IGNORE : activation;
        this.setChanged();
    }

    public boolean isDisabled(Level level) { return this.redstoneActivation.isDisabled(level, this.worldPosition); }
    protected boolean isActive() { return this.status.type().isActive(); }
    public boolean areDropsDisabled() { return this.disableDrops; }

    public long getEnergyItemInsertionRate() { return (long) (this.energyStorage.getCapacity() / 160.0); }
    public long getEnergyItemExtractionRate() { return (long) (this.energyStorage.getCapacity() / 160.0); }

    public final void tickBase(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                               @NotNull ProfilerFiller profiler) {
        this.setBlockState(state);
        if (level.isClientSide()) {
            this.tickClient(level, pos, state);
            return;
        }
        ServerLevel server = (ServerLevel) level;
        profiler.push("constant");
        this.tickConstant(server, pos, state, profiler);
        if (this.isDisabled(level)) {
            profiler.popPush("disabled");
            this.setStatus(MachineStatuses.OFF);
            this.tickDisabled(server, pos, state, profiler);
        } else {
            profiler.popPush("active");
            this.setStatus(this.tick(server, pos, state, profiler));
        }
        profiler.pop();
    }

    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {}
    protected void tickDisabled(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {}
    protected void tickClient(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {}
    protected abstract @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                                    @NotNull BlockState state, @NotNull ProfilerFiller profiler);

    /** Move energy from this machine into the first item of the named transfer group. */
    protected void drainPowerToStack(@NotNull SlotGroupType type) {
        if (this.energyStorage.isEmpty()) return;
        SlotGroup<net.minecraft.world.item.Item, ItemStack, ItemResourceSlot> group = this.itemStorage.getGroup(type);
        if (group.size() == 0) return;
        ItemResourceSlot slot = group.getSlot(0);
        ItemStack stack = slot.toStack();
        if (stack.isEmpty()) return;
        IEnergyStorage target = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (target == null || !target.canReceive()) return;
        long available = this.energyStorage.tryExtract(this.getEnergyItemInsertionRate());
        int offered = saturatingInt(available);
        int accepted = target.receiveEnergy(offered, false);
        if (accepted > 0) {
            this.energyStorage.extract(accepted);
            slot.setStack(stack);
        }
    }

    /** Move energy from the first item of the named transfer group into this machine. */
    protected void chargeFromStack(@NotNull SlotGroupType type) {
        if (this.energyStorage.isFull()) return;
        SlotGroup<net.minecraft.world.item.Item, ItemStack, ItemResourceSlot> group = this.itemStorage.getGroup(type);
        if (group.size() == 0) return;
        ItemResourceSlot slot = group.getSlot(0);
        ItemStack stack = slot.toStack();
        if (stack.isEmpty()) return;
        IEnergyStorage source = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (source == null || !source.canExtract()) return;
        long room = this.energyStorage.tryInsert(this.getEnergyItemExtractionRate());
        int requested = saturatingInt(room);
        int extracted = source.extractEnergy(requested, false);
        if (extracted > 0) {
            this.energyStorage.insert(extracted);
            slot.setStack(stack);
        }
    }

    /** Push energy into adjacent Forge Energy handlers. */
    protected void trySpreadEnergy(@NotNull ServerLevel level, @NotNull BlockState state) {
        IEnergyStorage source = this.energyStorage.getExposedStorage(ResourceFlow.OUTPUT);
        if (source == null || !source.canExtract()) return;
        for (Direction direction : Direction.values()) {
            if (source.getEnergyStored() <= 0) break;
            BlockEntity neighbor = level.getBlockEntity(this.worldPosition.relative(direction));
            if (neighbor == null) continue;
            IEnergyStorage target = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (target == null || !target.canReceive()) continue;
            int simulated = source.extractEnergy(Integer.MAX_VALUE, true);
            if (simulated <= 0) continue;
            int accepted = target.receiveEnergy(simulated, true);
            if (accepted <= 0) continue;
            int extracted = source.extractEnergy(accepted, false);
            if (extracted > 0) target.receiveEnergy(extracted, false);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        LazyOptional<T> local = this.capabilities.getCapability(cap, side);
        return local.isPresent() ? local : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        this.capabilities.invalidate();
        super.invalidateCaps();
    }

    @Override public Component getDisplayName() { return this.name; }
    @Override public abstract @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player);

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("MachineLibEnergy", this.energyStorage.getAmount());
        tag.put("MachineLibItems", this.itemStorage.createTag());
        tag.put("MachineLibFluids", this.fluidStorage.createTag());
        tag.putBoolean("MachineLibDisableDrops", this.disableDrops);
        tag.putByte("MachineLibRedstone", (byte) this.redstoneActivation.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("MachineLibEnergy")) this.energyStorage.setEnergy(tag.getLong("MachineLibEnergy"));
        if (tag.contains("MachineLibItems", Tag.TAG_LIST)) this.itemStorage.readTag(tag.getList("MachineLibItems", Tag.TAG_COMPOUND));
        if (tag.contains("MachineLibFluids", Tag.TAG_LIST)) this.fluidStorage.readTag(tag.getList("MachineLibFluids", Tag.TAG_COMPOUND));
        this.disableDrops = tag.getBoolean("MachineLibDisableDrops");
        int redstone = tag.getByte("MachineLibRedstone");
        if (redstone >= 0 && redstone < RedstoneActivation.values().length)
            this.redstoneActivation = RedstoneActivation.values()[redstone];
    }

    private static int saturatingInt(long amount) {
        if (amount <= 0) return 0;
        return amount >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }
}
