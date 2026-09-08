/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.machine;

import dev.galacticraft.api.accessor.LevelOxygenAccessor;
import dev.galacticraft.api.gas.Gases;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.Galacticraft;
import dev.galacticraft.mod.content.GCEntityTypes;
import dev.galacticraft.mod.content.GCMachineTypes;
import dev.galacticraft.mod.content.entity.BubbleEntity;
import dev.galacticraft.mod.machine.GCMachineStatuses;
import dev.galacticraft.mod.screen.OxygenBubbleDistributorMenu;
import dev.galacticraft.mod.util.FluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 overlay for Galacticraft's oxygen bubble distributor. */
public class OxygenBubbleDistributorBlockEntity extends MachineBlockEntity {
    public static final int CHARGE_SLOT = 0;
    public static final int OXYGEN_INPUT_SLOT = 1;
    public static final int OXYGEN_TANK = 0;
    public static final long MAX_OXYGEN = FluidUtil.bucketsToDroplets(50);

    public boolean bubbleVisible = true;
    private double size;
    private byte targetSize = 1;
    private int bubbleId = -1;

    public OxygenBubbleDistributorBlockEntity(BlockPos pos, BlockState state) {
        super(GCMachineTypes.OXYGEN_BUBBLE_DISTRIBUTOR, pos, state);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        profiler.push("extract_resources");
        this.chargeFromStack(CHARGE_SLOT);
        this.takeOxygenFromStack();
        profiler.pop();
    }

    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        profiler.push("bubble_distributor");
        MachineStatus status;
        this.distributeOxygenToArea(this.size, false);
        try {
            long energy = Galacticraft.CONFIG_MANAGER.get().oxygenCollectorEnergyConsumptionRate();
            if (this.energyStorage().canExtract(energy)) {
                if (this.size > this.targetSize) {
                    this.setSize(Math.max(this.size - 0.1F, this.targetSize));
                }

                if (this.size > 0.0D && this.bubbleVisible && this.bubbleId == -1) {
                    BubbleEntity entity = GCEntityTypes.BUBBLE.create(level);
                    if (entity != null) {
                        entity.setPosRaw(pos.getX(), pos.getY(), pos.getZ());
                        entity.xo = pos.getX();
                        entity.yo = pos.getY();
                        entity.zo = pos.getZ();
                        level.addFreshEntity(entity);
                        this.bubbleId = entity.getId();
                    }
                }

                long oxygenRequired = (long) ((4.0 / 3.0) * Math.PI * this.size * this.size * this.size);
                FluidResourceSlot slot = this.fluidStorage().getSlot(OXYGEN_TANK);
                if (slot.canExtract(oxygenRequired)) {
                    slot.extract(oxygenRequired);
                    this.energyStorage().extract(energy);
                    if (this.size < this.targetSize) {
                        this.setSize(this.size + 0.05D);
                    }
                    this.distributeOxygenToArea(this.size, true);
                    return GCMachineStatuses.DISTRIBUTING;
                }
                status = GCMachineStatuses.NOT_ENOUGH_OXYGEN;
            } else {
                status = MachineStatuses.NOT_ENOUGH_ENERGY;
            }
        } finally {
            profiler.pop();
        }

        Entity bubble = this.bubbleId == -1 ? null : level.getEntity(this.bubbleId);
        if (this.bubbleId != -1 && this.size <= 0) {
            if (bubble != null) bubble.remove(Entity.RemovalReason.DISCARDED);
            this.bubbleId = -1;
        }

        if (this.size > 0) this.setSize(this.size - 0.2D);
        if (this.size < 0) this.setSize(0);
        return status;
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

    public int getDistanceFromServer(int x, int y, int z) {
        int dx = this.getBlockPos().getX() - x;
        int dy = this.getBlockPos().getY() - y;
        int dz = this.getBlockPos().getZ() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public void distributeOxygenToArea(double size, boolean oxygenated) {
        if (this.level == null) return;
        LevelOxygenAccessor oxygen = (LevelOxygenAccessor) this.level;
        int radius = Mth.floor(size) + 4;
        int bubbleR2 = (int) (size * size);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = this.getBlockPos().getX() - radius; x <= this.getBlockPos().getX() + radius; x++) {
            for (int y = this.getBlockPos().getY() - radius; y <= this.getBlockPos().getY() + radius; y++) {
                for (int z = this.getBlockPos().getZ() - radius; z <= this.getBlockPos().getZ() + radius; z++) {
                    if (this.getDistanceFromServer(x, y, z) <= bubbleR2) {
                        oxygen.setBreathable(mutable.set(x, y, z), oxygenated);
                    }
                }
            }
        }
    }

    public byte getTargetSize() {
        return this.targetSize;
    }

    public void setTargetSize(byte targetSize) {
        this.targetSize = targetSize;
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte(Constant.Nbt.MAX_SIZE, this.targetSize);
        tag.putDouble(Constant.Nbt.SIZE, this.size);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.size = Math.max(0, nbt.getDouble(Constant.Nbt.SIZE));
        this.targetSize = nbt.getByte(Constant.Nbt.MAX_SIZE);
        if (this.targetSize < 1) this.targetSize = 1;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putByte(Constant.Nbt.MAX_SIZE, this.targetSize);
        tag.putDouble(Constant.Nbt.SIZE, this.size);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public double getSize() {
        return this.size;
    }

    public void setSize(double size) {
        if (Double.compare(this.size, size) == 0) return;
        this.size = size;
        this.setChanged();
        this.syncToClient();
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        if (this.getSecurity().hasAccess(player)) {
            return new OxygenBubbleDistributorMenu(syncId, (ServerPlayer) player, this);
        }
        return null;
    }
}
