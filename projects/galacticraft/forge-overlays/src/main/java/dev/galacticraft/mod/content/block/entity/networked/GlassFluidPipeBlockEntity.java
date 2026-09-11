/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.block.entity.networked;

import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.api.block.entity.Colored;
import dev.galacticraft.mod.api.block.entity.Connected;
import dev.galacticraft.mod.api.block.entity.Pullable;
import dev.galacticraft.mod.api.pipe.Pipe;
import dev.galacticraft.mod.content.GCBlockEntityTypes;
import dev.galacticraft.mod.content.block.special.fluidpipe.PipeBlockEntity;
import dev.galacticraft.mod.util.FluidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/** Forge recompilation removing the Fabric FluidConstants linkage. */
public class GlassFluidPipeBlockEntity extends PipeBlockEntity implements Colored, Connected, Pullable {
    private static final long TRANSFER_RATE = 1_620L; // 81,000 / 50 = 0.4 bucket/second
    private boolean pull;

    public GlassFluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(GCBlockEntityTypes.GLASS_FLUID_PIPE, pos, state, TRANSFER_RATE);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.readPullNbt(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        this.writePullNbt(nbt);
    }

    @Override
    public boolean isPull() {
        return this.pull;
    }

    @Override
    public void setPull(boolean pull) {
        this.pull = pull;
    }

    @Override
    public void calculateConnections() {
        for (var direction : Constant.Misc.DIRECTIONS) {
            var otherBlockEntity = this.level.getBlockEntity(this.getBlockPos().relative(direction));
            this.getConnections()[direction.ordinal()] = otherBlockEntity instanceof Pipe pipe && pipe.canConnect(direction.getOpposite()) || FluidUtil.canAccessFluid(this.level, this.getBlockPos().relative(direction), direction);
        }
    }
}
