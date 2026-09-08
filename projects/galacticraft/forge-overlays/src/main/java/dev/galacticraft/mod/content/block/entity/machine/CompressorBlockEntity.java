/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.machine;

import dev.galacticraft.machinelib.api.block.entity.BasicRecipeMachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.GCMachineTypes;
import dev.galacticraft.mod.machine.GCMachineStatuses;
import dev.galacticraft.mod.recipe.CompressingRecipe;
import dev.galacticraft.mod.recipe.GCRecipes;
import dev.galacticraft.mod.screen.CompressorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forge 1.20.1 overlay for Galacticraft's solid-fuel compressor. */
public class CompressorBlockEntity extends BasicRecipeMachineBlockEntity<Container, CompressingRecipe> {
    public static final int FUEL_SLOT = 0;
    public static final int INPUT_SLOTS = 1;
    public static final int INPUT_LENGTH = 9;
    public static final int OUTPUT_SLOT = INPUT_SLOTS + INPUT_LENGTH;

    public int fuelTime;
    public int fuelLength;

    private long fuelSlotModification = -1;
    private boolean hasFuel;

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(GCMachineTypes.COMPRESSOR, pos, state, GCRecipes.COMPRESSING_TYPE,
                INPUT_SLOTS, INPUT_LENGTH, OUTPUT_SLOT);
    }

    @Override
    protected @NotNull MachineStatus workingStatus(CompressingRecipe recipe) {
        return GCMachineStatuses.COMPRESSING;
    }

    @Override
    protected @Nullable MachineStatus hasResourcesToWork() {
        if (this.fuelLength == 0) {
            ItemResourceSlot slot = this.itemStorage().getSlot(FUEL_SLOT);
            if (slot.getModifications() != this.fuelSlotModification) {
                this.fuelSlotModification = slot.getModifications();
                this.hasFuel = getBurnTime(slot) > 0;
            }
            return this.hasFuel ? null : GCMachineStatuses.NO_FUEL;
        }
        return null;
    }

    @Override
    protected void extractResourcesToWork() {
        if (this.fuelLength != 0) return;

        ItemResourceSlot slot = this.itemStorage().getSlot(FUEL_SLOT);
        int time = getBurnTime(slot);
        if (time > 0 && slot.consumeOne() != null) {
            this.fuelTime = this.fuelLength = time;
        }
    }

    /**
     * Forge fuel compatibility: allow stack-sensitive/modded fuel overrides first,
     * then fall back to vanilla's furnace fuel table when Forge returns -1.
     */
    private static int getBurnTime(@NotNull ItemResourceSlot slot) {
        ItemStack stack = slot.toStack();
        if (stack.isEmpty()) return 0;

        int forgeTime = stack.getBurnTime(RecipeType.SMELTING);
        if (forgeTime >= 0) return forgeTime;
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(), 0);
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel world, @NotNull BlockPos pos,
                                @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(world, pos, state, profiler);
        if (--this.fuelTime <= 0) {
            this.fuelLength = 0;
            this.fuelTime = 0;
        }
    }

    @Override
    public @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos,
                                       @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        CompressingRecipe recipe = this.getActiveRecipe();
        if (recipe != null && this.getState().isActive()) {
            int maxProgress = this.getProcessingTime(recipe);
            if (maxProgress > 0 && this.getProgress() % Math.max(1, maxProgress / 8) == 0
                    && this.getProgress() > maxProgress / 2) {
                level.playSound(null, this.getBlockPos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS,
                        0.5F, level.random.nextFloat() * 0.1F + 0.9F);
            }
        }
        return super.tick(level, pos, state, profiler);
    }

    @Override
    public int getProcessingTime(@NotNull CompressingRecipe recipe) {
        return recipe.getTime();
    }

    public int getFuelTime() {
        return this.fuelTime;
    }

    public int getFuelLength() {
        return this.fuelLength;
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(Constant.Nbt.FUEL_TIME, this.fuelTime);
        tag.putInt(Constant.Nbt.FUEL_LENGTH, this.fuelLength);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.fuelTime = nbt.getInt(Constant.Nbt.FUEL_TIME);
        this.fuelLength = nbt.getInt(Constant.Nbt.FUEL_LENGTH);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        if (this.getSecurity().hasAccess(player)) {
            return new CompressorMenu(syncId, (ServerPlayer) player, this);
        }
        return null;
    }
}
