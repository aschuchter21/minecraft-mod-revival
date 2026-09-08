/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.api.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Forge 1.20.1 gas fluid implementation without Fabric Transfer API linkage.
 * The built-in Galacticraft gases are non-placeable storage resources, so their
 * Forge FluidType carries the loader-facing density/viscosity metadata while this
 * class preserves Galacticraft's name, symbol, texture and Fluid behavior.
 */
@ApiStatus.Experimental
@Deprecated
public final class GasFluid extends Fluid implements Gas {
    @ApiStatus.Internal
    public static final List<GasFluid> GAS_FLUIDS = new ArrayList<>();

    private final @NotNull Component name;
    private final @NotNull String symbol;
    private final @NotNull ResourceLocation texture;
    private final int tint;
    private final @NotNull FluidType fluidType;

    private GasFluid(@NotNull Component name, @NotNull ResourceLocation texture,
                     @NotNull String symbol, int tint) {
        this.name = name;
        this.texture = texture;
        this.symbol = symbol
                .replace("0", "₀").replace("1", "₁").replace("2", "₂")
                .replace("3", "₃").replace("4", "₄").replace("5", "₅")
                .replace("6", "₆").replace("7", "₇").replace("8", "₈").replace("9", "₉");
        this.tint = tint;
        this.fluidType = new FluidType(FluidType.Properties.create()
                .density(-100)
                .viscosity(50)
                .canDrown(false)
                .canSwim(false)
                .canPushEntity(false)) {
            @Override
            public Component getDescription() {
                return GasFluid.this.name;
            }
        };
        GAS_FLUIDS.add(this);
    }

    public static @NotNull GasFluid create(@NotNull Component name,
                                            @NotNull ResourceLocation texture,
                                            @NotNull String symbol) {
        return new GasFluid(name, texture, symbol, 0xFFFFFFFF);
    }

    public static @NotNull GasFluid create(@NotNull Component name,
                                            @NotNull ResourceLocation texture,
                                            @NotNull String symbol,
                                            int tint) {
        return new GasFluid(name, texture, symbol, tint);
    }

    @Override
    public @NotNull FluidType getFluidType() {
        return this.fluidType;
    }

    @Override
    public @NotNull Item getBucket() {
        return Items.AIR;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos,
                                        Fluid fluid, Direction direction) {
        return true;
    }

    @Override
    protected @NotNull Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 0;
    }

    @Override
    protected float getExplosionResistance() {
        return 0;
    }

    @Override
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return 0;
    }

    @Override
    protected @NotNull BlockState createLegacyBlock(FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }

    @Override
    public int getAmount(FluidState state) {
        return 0;
    }

    @Override
    public @NotNull VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    public @NotNull ResourceLocation getTexture() {
        return this.texture;
    }

    public int getTint() {
        return this.tint;
    }

    @Override
    public @NotNull Component getName() {
        return this.name;
    }

    @Override
    public @NotNull String getSymbol() {
        return this.symbol;
    }
}
