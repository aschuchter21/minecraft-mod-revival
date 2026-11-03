/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.network;

import dev.galacticraft.api.accessor.GearInventoryProvider;
import dev.galacticraft.mod.Constant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Dedicated Forge S2C channel for the original 12 Galacticraft gear slots.
 * The server owns inventory state; clients never send arbitrary gear contents.
 */
public final class ForgeGearNetworking {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Constant.id("gear_forge"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final int MAX_SLOTS = 12;

    private ForgeGearNetworking() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) return;
        CHANNEL.registerMessage(0, SyncGearPacket.class,
                SyncGearPacket::encode, SyncGearPacket::decode, SyncGearPacket::handle);
    }

    public static void sendToTracking(ServerPlayer owner) {
        register();
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> owner), snapshot(owner));
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer owner) {
            sendToTracking(owner);
        }
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer owner) {
            sendToTracking(owner);
        }
    }

    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer owner) {
            sendToTracking(owner);
        }
    }

    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer watcher
                && event.getTarget() instanceof ServerPlayer owner) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> watcher), snapshot(owner));
        }
    }

    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof GearInventoryProvider original)
                || !(event.getEntity() instanceof GearInventoryProvider replacement)) return;
        Container oldInventory = original.getGearInv();
        Container newInventory = replacement.getGearInv();
        for (int i = 0; i < Math.min(oldInventory.getContainerSize(), newInventory.getContainerSize()); i++) {
            newInventory.setItem(i, oldInventory.getItem(i).copy());
        }
    }

    private static SyncGearPacket snapshot(Player owner) {
        if (!(owner instanceof GearInventoryProvider provider)) {
            throw new IllegalStateException("Galacticraft Player gear mixin did not apply");
        }
        Container gear = provider.getGearInv();
        List<ItemStack> stacks = new ArrayList<>(gear.getContainerSize());
        for (int i = 0; i < gear.getContainerSize(); i++) {
            stacks.add(gear.getItem(i).copy());
        }
        return new SyncGearPacket(owner.getId(), stacks);
    }

    public record SyncGearPacket(int entityId, List<ItemStack> stacks) {
        private static void encode(SyncGearPacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.entityId);
            buffer.writeVarInt(packet.stacks.size());
            packet.stacks.forEach(buffer::writeItem);
        }

        private static SyncGearPacket decode(FriendlyByteBuf buffer) {
            int entityId = buffer.readVarInt();
            int size = buffer.readVarInt();
            if (size < 0 || size > MAX_SLOTS) {
                throw new IllegalArgumentException("Invalid Galacticraft gear slot count: " + size);
            }
            List<ItemStack> stacks = new ArrayList<>(size);
            for (int i = 0; i < size; i++) stacks.add(buffer.readItem());
            return new SyncGearPacket(entityId, stacks);
        }

        private static void handle(SyncGearPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT, () -> () -> ForgeGearClientPackets.apply(packet)
            ));
            context.setPacketHandled(true);
        }
    }
}
