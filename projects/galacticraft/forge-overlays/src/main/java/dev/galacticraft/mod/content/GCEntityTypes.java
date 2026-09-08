/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.content;

import dev.galacticraft.api.entity.attribute.GcApiEntityAttributes;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.entity.ArchGreyEntity;
import dev.galacticraft.mod.content.entity.BubbleEntity;
import dev.galacticraft.mod.content.entity.CometCubeEntity;
import dev.galacticraft.mod.content.entity.EvolvedCreeperEntity;
import dev.galacticraft.mod.content.entity.EvolvedEvokerEntity;
import dev.galacticraft.mod.content.entity.EvolvedPillagerEntity;
import dev.galacticraft.mod.content.entity.EvolvedSkeletonEntity;
import dev.galacticraft.mod.content.entity.EvolvedSpiderEntity;
import dev.galacticraft.mod.content.entity.EvolvedVindicatorEntity;
import dev.galacticraft.mod.content.entity.EvolvedZombieEntity;
import dev.galacticraft.mod.content.entity.GazerEntity;
import dev.galacticraft.mod.content.entity.GreyEntity;
import dev.galacticraft.mod.content.entity.LanderEntity;
import dev.galacticraft.mod.content.entity.OliGrubEntity;
import dev.galacticraft.mod.content.entity.RocketEntity;
import dev.galacticraft.mod.content.entity.RumblerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Forge 1.20.1 replacement for FabricEntityTypeBuilder and
 * FabricDefaultAttributeRegistry usage in the recovered 1.20.1 source.
 */
public final class GCEntityTypes {
    public static final EntityType<EvolvedZombieEntity> EVOLVED_ZOMBIE = build(
            Constant.Entity.EVOLVED_ZOMBIE,
            EntityType.Builder.of(EvolvedZombieEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
    public static final EntityType<EvolvedCreeperEntity> EVOLVED_CREEPER = build(
            Constant.Entity.EVOLVED_CREEPER,
            EntityType.Builder.of(EvolvedCreeperEntity::new, MobCategory.MONSTER).sized(0.65F, 1.8F));
    public static final EntityType<EvolvedSkeletonEntity> EVOLVED_SKELETON = build(
            Constant.Entity.EVOLVED_SKELETON,
            EntityType.Builder.of(EvolvedSkeletonEntity::new, MobCategory.MONSTER).sized(0.6F, 1.99F));
    public static final EntityType<EvolvedSpiderEntity> EVOLVED_SPIDER = build(
            Constant.Entity.EVOLVED_SPIDER,
            EntityType.Builder.of(EvolvedSpiderEntity::new, MobCategory.MONSTER).sized(1.4F, 0.9F));
    public static final EntityType<EvolvedPillagerEntity> EVOLVED_PILLAGER = build(
            Constant.Entity.EVOLVED_PILLAGER,
            EntityType.Builder.of(EvolvedPillagerEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
    public static final EntityType<EvolvedEvokerEntity> EVOLVED_EVOKER = build(
            Constant.Entity.EVOLVED_EVOKER,
            EntityType.Builder.of(EvolvedEvokerEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
    public static final EntityType<EvolvedVindicatorEntity> EVOLVED_VINDICATOR = build(
            Constant.Entity.EVOLVED_VINDICATOR,
            EntityType.Builder.of(EvolvedVindicatorEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F));
    public static final EntityType<GazerEntity> GAZER = build(
            Constant.Entity.GAZER,
            EntityType.Builder.of(GazerEntity::new, MobCategory.MONSTER).sized(2.0F, 3.0F));
    public static final EntityType<RumblerEntity> RUMBLER = build(
            Constant.Entity.RUMBLER,
            EntityType.Builder.of(RumblerEntity::new, MobCategory.MONSTER).sized(1.0F, 1.55F));
    public static final EntityType<CometCubeEntity> COMET_CUBE = build(
            Constant.Entity.COMET_CUBE,
            EntityType.Builder.of(CometCubeEntity::new, MobCategory.MONSTER).sized(1.0F, 1.55F));
    public static final EntityType<OliGrubEntity> OLI_GRUB = build(
            Constant.Entity.OLI_GRUB,
            EntityType.Builder.of(OliGrubEntity::new, MobCategory.CREATURE).sized(1.0F, 1.55F));
    public static final EntityType<GreyEntity> GREY = build(
            Constant.Entity.GREY,
            EntityType.Builder.of(GreyEntity::new, MobCategory.CREATURE).sized(0.6F, 1.55F));
    public static final EntityType<ArchGreyEntity> ARCH_GREY = build(
            Constant.Entity.ARCH_GREY,
            EntityType.Builder.of(ArchGreyEntity::new, MobCategory.CREATURE).sized(0.6F, 1.55F));

    public static final EntityType<BubbleEntity> BUBBLE = build(
            Constant.Entity.BUBBLE,
            EntityType.Builder.of(BubbleEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.0F, 0.0F).noSave().noSummon());

    public static final EntityType<RocketEntity> ROCKET = build(
            Constant.Entity.ROCKET,
            EntityType.Builder.of(RocketEntity::new, MobCategory.MISC)
                    .clientTrackingRange(32)
                    .updateInterval(2)
                    .setShouldReceiveVelocityUpdates(false)
                    .sized(2.3F, 5.25F));

    public static final EntityType<LanderEntity> LANDER = build(
            Constant.Entity.LANDER,
            EntityType.Builder.of(LanderEntity::new, MobCategory.MISC)
                    .clientTrackingRange(32)
                    .sized(2.5F, 4.0F)
                    .fireImmune());

    private GCEntityTypes() {
    }

    private static <T extends Entity> EntityType<T> build(String id, EntityType.Builder<T> builder) {
        return builder.build(Constant.id(id).toString());
    }

    /** Original common entrypoint retained; Forge owns registry timing. */
    public static void register() {
    }

    public static void register(RegisterEvent event) {
        register(event, Constant.Entity.EVOLVED_ZOMBIE, EVOLVED_ZOMBIE);
        register(event, Constant.Entity.EVOLVED_CREEPER, EVOLVED_CREEPER);
        register(event, Constant.Entity.EVOLVED_SKELETON, EVOLVED_SKELETON);
        register(event, Constant.Entity.EVOLVED_SPIDER, EVOLVED_SPIDER);
        register(event, Constant.Entity.EVOLVED_PILLAGER, EVOLVED_PILLAGER);
        register(event, Constant.Entity.EVOLVED_EVOKER, EVOLVED_EVOKER);
        register(event, Constant.Entity.EVOLVED_VINDICATOR, EVOLVED_VINDICATOR);
        register(event, Constant.Entity.GAZER, GAZER);
        register(event, Constant.Entity.RUMBLER, RUMBLER);
        register(event, Constant.Entity.COMET_CUBE, COMET_CUBE);
        register(event, Constant.Entity.OLI_GRUB, OLI_GRUB);
        register(event, Constant.Entity.GREY, GREY);
        register(event, Constant.Entity.ARCH_GREY, ARCH_GREY);
        register(event, Constant.Entity.BUBBLE, BUBBLE);
        register(event, Constant.Entity.ROCKET, ROCKET);
        register(event, Constant.Entity.LANDER, LANDER);
    }

    private static void register(RegisterEvent event, String id, EntityType<?> type) {
        event.register(Registries.ENTITY_TYPE, Constant.id(id), () -> type);
    }

    /** Forge replacement for FabricDefaultAttributeRegistry. */
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EVOLVED_ZOMBIE, EvolvedZombieEntity.createAttributes()
                .add(GcApiEntityAttributes.CAN_BREATHE_IN_SPACE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.MAX_HEALTH, 30.0D)
                .build());
        event.put(EVOLVED_CREEPER, EvolvedCreeperEntity.createAttributes()
                .add(GcApiEntityAttributes.CAN_BREATHE_IN_SPACE, 1.0D).build());
        event.put(EVOLVED_SKELETON, EvolvedSkeletonEntity.createAttributes()
                .add(GcApiEntityAttributes.CAN_BREATHE_IN_SPACE, 1.0D)
                .add(Attributes.MAX_HEALTH, 25.0D).build());
        event.put(EVOLVED_SPIDER, EvolvedSpiderEntity.createAttributes()
                .add(GcApiEntityAttributes.CAN_BREATHE_IN_SPACE, 1.0D)
                .add(Attributes.MAX_HEALTH, 22.0D).build());
        event.put(EVOLVED_PILLAGER, EvolvedPillagerEntity.createAttributes()
                .add(GcApiEntityAttributes.CAN_BREATHE_IN_SPACE, 1.0D)
                .add(Attributes.MAX_HEALTH, 25.0D).build());
        event.put(EVOLVED_EVOKER, EvolvedEvokerEntity.createAttributes()
                .add(GcApiEntityAttributes.CAN_BREATHE_IN_SPACE, 1.0D)
                .add(Attributes.MAX_HEALTH, 25.0D).build());
        event.put(EVOLVED_VINDICATOR, EvolvedVindicatorEntity.createAttributes()
                .add(GcApiEntityAttributes.CAN_BREATHE_IN_SPACE, 1.0D)
                .add(Attributes.MAX_HEALTH, 25.0D).build());
        event.put(GAZER, GazerEntity.createAttributes().build());
        event.put(RUMBLER, RumblerEntity.createAttributes().build());
        event.put(COMET_CUBE, CometCubeEntity.createAttributes().build());
        event.put(OLI_GRUB, OliGrubEntity.createAttributes().build());
        event.put(GREY, GreyEntity.createAttributes().build());
        event.put(ARCH_GREY, ArchGreyEntity.createAttributes().build());
    }
}
