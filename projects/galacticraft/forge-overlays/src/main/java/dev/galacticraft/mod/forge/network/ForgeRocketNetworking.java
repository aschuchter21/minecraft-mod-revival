/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.network;

import dev.galacticraft.api.accessor.SatelliteAccessor;
import dev.galacticraft.api.registry.AddonRegistries;
import dev.galacticraft.api.rocket.RocketData;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.api.universe.celestialbody.landable.Landable;
import dev.galacticraft.api.universe.celestialbody.landable.teleporter.CelestialTeleporter;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.forge.compat.ForgeRocketTravelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

/** Forge replacement for Galacticraft's Fabric rocket/celestial packets. */
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
        CHANNEL.registerMessage(
                1,
                SelectPlanetPacket.class,
                SelectPlanetPacket::encode,
                SelectPlanetPacket::decode,
                SelectPlanetPacket::handle
        );
    }

    public static void sendOpenPlanetMenu(ServerPlayer player, RocketData rocketData, int currentBodyId) {
        register();
        CompoundTag tag = rocketData.toNbt(new CompoundTag());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenPlanetMenuPacket(tag, currentBodyId));
    }

    /** Client-side entry point used by the Forge celestial selection screen. */
    public static void selectPlanet(ResourceLocation bodyId) {
        register();
        CHANNEL.sendToServer(new SelectPlanetPacket(bodyId));
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

    /**
     * Forge C2S replacement for Galacticraft's Fabric planet_tp packet.
     * The server remains authoritative: it resolves the requested body, checks
     * the rocket's travel predicates, consumes the stored orbit travel state and
     * only then invokes the body's registered celestial teleporter.
     */
    public record SelectPlanetPacket(ResourceLocation bodyId) {
        private static void encode(SelectPlanetPacket message, FriendlyByteBuf buf) {
            buf.writeResourceLocation(message.bodyId);
        }

        private static SelectPlanetPacket decode(FriendlyByteBuf buf) {
            return new SelectPlanetPacket(buf.readResourceLocation());
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static void handle(SelectPlanetPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            ServerPlayer player = context.getSender();
            if (player == null) {
                context.setPacketHandled(true);
                return;
            }

            context.enqueueWork(() -> {
                RocketData rocketData = ForgeRocketTravelState.get(player);
                if (rocketData == null) {
                    disconnectInvalidTeleport(player);
                    return;
                }

                CelestialBody<?, ?> fromBody = CelestialBody.getByDimension(player.level()).orElse(null);
                if (fromBody == null) {
                    disconnectInvalidTeleport(player);
                    return;
                }

                CelestialBody<?, ?> body = null;
                if (player.server instanceof SatelliteAccessor satellites) {
                    body = satellites.getSatellites().get(message.bodyId);
                }
                if (body == null) {
                    body = player.server.registryAccess()
                            .registryOrThrow(AddonRegistries.CELESTIAL_BODY)
                            .get(message.bodyId);
                }

                if (body == null || !(body.type() instanceof Landable landable)
                        || !(rocketData.canTravel(player.server.registryAccess(), fromBody, body)
                        || rocketData == RocketData.empty())) {
                    disconnectInvalidTeleport(player);
                    return;
                }

                ServerLevel destination = player.server.getLevel(landable.world(body.config()));
                if (destination == null) {
                    disconnectInvalidTeleport(player);
                    return;
                }

                ForgeRocketTravelState.clear(player);
                CelestialTeleporter teleporter = (CelestialTeleporter) landable.teleporter(body.config()).value();
                teleporter.onEnterAtmosphere(destination, player, body, fromBody);
            });
            context.setPacketHandled(true);
        }

        private static void disconnectInvalidTeleport(ServerPlayer player) {
            ForgeRocketTravelState.clear(player);
            player.connection.disconnect(Component.literal("Invalid planet teleport packet received."));
        }
    }
}
