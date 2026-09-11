/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.networked;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.api.block.entity.Colored;
import dev.galacticraft.mod.api.block.entity.Walkway;
import dev.galacticraft.mod.api.pipe.Pipe;
import dev.galacticraft.mod.content.GCBlockEntityTypes;
import dev.galacticraft.mod.content.block.special.fluidpipe.PipeBlockEntity;
import dev.galacticraft.mod.util.FluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/** Forge recompilation removing the Fabric FluidConstants linkage. */
public class FluidPipeWalkwayBlockEntity extends PipeBlockEntity implements Walkway, Colored {
    private static final long TRANSFER_RATE = 1_620L; // 81,000 / 50 = 0.4 bucket/second
    private Direction direction;

    public FluidPipeWalkwayBlockEntity(BlockPos pos, BlockState state) {
        super(GCBlockEntityTypes.FLUID_PIPE_WALKWAY, pos, state, TRANSFER_RATE);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        this.writeWalkwayNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.readWalkwayNbt(nbt);
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(@NotNull Direction direction) {
        this.direction = direction;
        this.getConnections()[direction.ordinal()] = false;

        if (this.hasLevel()) {
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    @Override
    public boolean canConnect(Direction direction) {
        return direction != this.direction;
    }

    @Override
    public void calculateConnections() {
        for (var direction : Constant.Misc.DIRECTIONS) {
            var otherBlockEntity = this.level.getBlockEntity(this.getBlockPos().relative(direction));
            this.getConnections()[direction.ordinal()] = otherBlockEntity instanceof Pipe pipe && pipe.canConnect(direction.getOpposite()) || FluidUtil.canAccessFluid(this.level, this.getBlockPos().relative(direction), direction);
        }
    }
}
