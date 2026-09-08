/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.network;

import dev.galacticraft.api.rocket.RocketData;
import dev.galacticraft.mod.Constant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/** Forge replacement for Galacticraft's Fabric rocket/celestial S2C packets. */
public final class ForgeRocketNetworking {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Constant.id("rocket_forge"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private ForgeRocketNetworking() {
    }

    /** Must be invoked by the Forge bootstrap before players connect. */
    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) return;
        CHANNEL.registerMessage(
                0,
                OpenPlanetMenuPacket.class,
                OpenPlanetMenuPacket::encode,
                OpenPlanetMenuPacket::decode,
                OpenPlanetMenuPacket::handle
        );
    }

    public static void sendOpenPlanetMenu(ServerPlayer player, RocketData rocketData, int currentBodyId) {
        register();
        CompoundTag tag = rocketData.toNbt(new CompoundTag());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenPlanetMenuPacket(tag, currentBodyId));
    }

    public record OpenPlanetMenuPacket(CompoundTag rocketData, int currentBodyId) {
        private static void encode(OpenPlanetMenuPacket message, FriendlyByteBuf buf) {
            buf.writeNbt(message.rocketData);
            buf.writeInt(message.currentBodyId);
        }

        private static OpenPlanetMenuPacket decode(FriendlyByteBuf buf) {
            return new OpenPlanetMenuPacket(Objects.requireNonNull(buf.readNbt()), buf.readInt());
        }

        private static void handle(OpenPlanetMenuPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> ForgeRocketClientPackets.openPlanetMenu(message)
            ));
            context.setPacketHandled(true);
        }
    }
}
