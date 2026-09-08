/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content.entity;

import com.mojang.datafixers.util.Pair;
import dev.galacticraft.mod.particle.GCParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Forge 1.20.1 lander entity.
 *
 * <p>The recovered Fabric implementation simulated the vehicle on the client and
 * sent an unrestricted full entity transform packet every tick. Forge can use the
 * normal tracked-entity channel instead: physics are authoritative on the server,
 * and vanilla entity synchronization mirrors position/rotation to clients.</p>
 */
public class LanderEntity extends Entity {
    protected long ticks;

    public LanderEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ticks = tag.getLong("LanderTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("LanderTicks", this.ticks);
    }

    public Pair<Vec3, Vec3> getParticlePosition() {
        double sinPitch = Math.sin(this.getXRot() / 180D / Math.PI);
        double x1 = 4 * Math.cos(this.getYRot() / 180D / Math.PI) * sinPitch;
        double z1 = 4 * Math.sin(this.getYRot() / 180D / Math.PI) * sinPitch;
        double y1 = -4 * Math.abs(Math.cos(this.getXRot() / 180D / Math.PI));
        double motionY = this.getDeltaMovement().y();
        return Pair.of(
                new Vec3(this.getX(), this.getY() + 1D + motionY / 2D, this.getZ()),
                new Vec3(x1, y1 + motionY / 2D, z1)
        );
    }

    @Override
    public void tick() {
        super.tick();
        this.ticks++;

        if (!this.level().isClientSide()) {
            this.tickServerPhysics();
        } else if (!this.onGround()) {
            Pair<Vec3, Vec3> particle = this.getParticlePosition();
            Vec3 pos = particle.getFirst();
            Vec3 motion = particle.getSecond();
            this.level().addParticle(
                    GCParticleTypes.LANDER_FLAME_PARTICLE,
                    pos.x(), pos.y(), pos.z(),
                    motion.x(), motion.y(), motion.z()
            );
        }
    }

    private void tickServerPhysics() {
        if (this.onGround()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        Vec3 current = super.getDeltaMovement();
        double vertical;
        if (this.ticks < 40) {
            vertical = 0.0D;
        } else if (this.ticks < 45) {
            vertical = -2.5D;
        } else {
            vertical = current.y() - 0.008D;
        }

        double pitchFactor = -Math.sin(this.getXRot() / 180D / Math.PI);
        double motionX = Math.cos(this.getYRot() / 180D / Math.PI) * pitchFactor / 2.0D;
        double motionZ = Math.sin(this.getYRot() / 180D / Math.PI) * pitchFactor / 2.0D;

        this.setDeltaMovement(motionX, vertical, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hit, InteractionHand hand) {
        player.startRiding(this);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.getPassengers().isEmpty()) {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
