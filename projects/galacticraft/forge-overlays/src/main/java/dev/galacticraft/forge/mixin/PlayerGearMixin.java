/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.forge.mixin;

import dev.galacticraft.api.accessor.GearInventoryProvider;
import dev.galacticraft.impl.internal.inventory.MappedInventory;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.world.inventory.GearInventory;
import dev.galacticraft.mod.forge.network.ForgeGearNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge implementation of the original Galacticraft player gear contract.
 * Original compiled screens and machine code refer directly to these methods
 * on Player, so a capability alone cannot preserve that binary interface.
 */
@Mixin(Player.class)
public abstract class PlayerGearMixin implements GearInventoryProvider {
    @Unique private final GearInventory galacticraft$gear = new GearInventory();
    @Unique private final Container galacticraft$tanks =
            MappedInventory.create(this.galacticraft$gear, 4, 5);
    @Unique private final Container galacticraft$thermal =
            MappedInventory.create(this.galacticraft$gear, 0, 1, 2, 3);
    @Unique private final Container galacticraft$accessories =
            MappedInventory.create(this.galacticraft$gear, 6, 7, 8, 9, 10, 11);

    @Override
    public Container getGearInv() {
        return this.galacticraft$gear;
    }

    @Override
    public Container getOxygenTanks() {
        return this.galacticraft$tanks;
    }

    @Override
    public Container getThermalArmor() {
        return this.galacticraft$thermal;
    }

    @Override
    public Container getAccessories() {
        return this.galacticraft$accessories;
    }

    @Override
    public void writeGearToNbt(CompoundTag tag) {
        tag.put(Constant.Nbt.GEAR_INV, this.galacticraft$gear.createTag());
    }

    @Override
    public void readGearFromNbt(CompoundTag tag) {
        this.galacticraft$gear.fromTag(tag.getList(Constant.Nbt.GEAR_INV, Tag.TAG_COMPOUND));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void galacticraft$saveGear(CompoundTag tag, CallbackInfo callback) {
        this.writeGearToNbt(tag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void galacticraft$loadGear(CompoundTag tag, CallbackInfo callback) {
        if (tag.contains(Constant.Nbt.GEAR_INV, Tag.TAG_LIST)) {
            this.readGearFromNbt(tag);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void galacticraft$attachGearListener(CallbackInfo callback) {
        this.galacticraft$gear.addListener(inventory -> {
            if ((Object) this instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
                ForgeGearNetworking.sendToTracking(serverPlayer);
            }
        });
    }
}
