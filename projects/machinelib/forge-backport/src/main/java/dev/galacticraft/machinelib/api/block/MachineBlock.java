/*
 * Copyright (c) 2021-2023 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.machinelib.api.block;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Forge 1.20.1 compatibility port of MachineLib 0.3's base machine block.
 *
 * <p>The original class mixes common block behavior with Fabric/client tooltip helpers.
 * This port keeps the server/runtime contract Galacticraft depends on: active/facing
 * state, block-entity construction and ticking, ownership-aware menu opening, and
 * machine inventory drops.</p>
 */
public class MachineBlock<Machine extends MachineBlockEntity> extends BaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final MachineBlockEntityFactory<Machine> factory;

    public MachineBlock(Properties settings, MachineBlockEntityFactory<Machine> factory) {
        super(settings);
        this.factory = factory;
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    public static void updateActiveState(Level level, BlockPos pos, BlockState state, boolean active) {
        if (state.hasProperty(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    public static boolean isActive(@NotNull BlockState state) {
        return state.hasProperty(ACTIVE) && state.getValue(ACTIVE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING, ACTIVE);
    }

    @Override
    public @Nullable Machine newBlockEntity(BlockPos pos, BlockState state) {
        return this.factory.create(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(
                BlockStateProperties.HORIZONTAL_FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, level, pos, block, fromPos, notify);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            machine.getState().setPowered(level.hasNeighborSignal(pos));
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            machine.getState().setPowered(level.hasNeighborSignal(pos));
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public final @NotNull InteractionResult use(BlockState state, @NotNull Level level, BlockPos pos,
                                                Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            SecuritySettings security = machine.getSecurity();
            if (!security.hasOwner() && player instanceof ServerPlayer serverPlayer) {
                security.setOwner(serverPlayer.getUUID(), serverPlayer.getGameProfile().getName());
            }
            if (security.hasAccess(player)) {
                player.openMenu(machine);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!level.isClientSide && entity instanceof MachineBlockEntity machine && !machine.areDropsDisabled()) {
            MachineItemStorage inventory = machine.itemStorage();
            for (ItemResourceSlot slot : inventory.getSlots()) {
                if (slot.isEmpty()) continue;
                ItemStack stack = slot.toStack();
                if (!stack.isEmpty()) {
                    level.addFreshEntity(new ItemEntity(
                            level,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            stack.copy()
                    ));
                }
                slot.set(null, null, 0);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof MachineBlockEntity machine) {
                machine.tickBase(tickLevel, pos, tickState, tickLevel.getProfiler());
            }
        };
    }

    /** Loader-neutral description hook retained for Galacticraft subclasses. */
    public @Nullable Component shiftDescription(ItemStack stack, BlockGetter view, TooltipFlag context) {
        return Component.translatable(this.getDescriptionId() + ".description");
    }

    @FunctionalInterface
    public interface MachineBlockEntityFactory<Machine extends MachineBlockEntity> {
        @Nullable Machine create(@NotNull BlockPos pos, @NotNull BlockState state);
    }
}
