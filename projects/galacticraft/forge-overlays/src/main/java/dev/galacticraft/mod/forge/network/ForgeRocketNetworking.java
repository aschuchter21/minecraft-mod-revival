/*
 * Copyright (c) 2019-2023 Team Galacticraft
 * MIT License
 */
package dev.galacticraft.mod.forge.network;

import dev.galacticraft.api.accessor.SatelliteAccessor;
import dev.galacticraft.api.registry.AddonRegistries;
import dev.galacticraft.api.rocket.RocketData;
import dev.galacticraft.api.rocket.LaunchStage;
import dev.galacticraft.api.universe.celestialbody.CelestialBody;
import dev.galacticraft.api.universe.celestialbody.landable.Landable;
import dev.galacticraft.api.universe.celestialbody.landable.teleporter.CelestialTeleporter;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.content.entity.RocketEntity;
import dev.galacticraft.mod.content.entity.LanderEntity;
import dev.galacticraft.mod.forge.compat.ForgeRocketTravelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Objects;
import java.util.Map;
import java.util.WeakHashMap;
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
    private static final Map<ServerPlayer, Integer> LAST_INPUT_TICK = new WeakHashMap<>();
    public static final int JUMP = 1;
    public static final int UP = 1 << 1;
    public static final int DOWN = 1 << 2;
    public static final int LEFT = 1 << 3;
    public static final int RIGHT = 1 << 4;
    public static final int SNEAK = 1 << 5;
    private static final int VALID_INPUT_MASK = JUMP | UP | DOWN | LEFT | RIGHT | SNEAK;

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
        CHANNEL.registerMessage(
                2,
                VehicleInputPacket.class,
                VehicleInputPacket::encode,
                VehicleInputPacket::decode,
                VehicleInputPacket::handle
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

    /**
     * Send the local rider's controls; the server is responsible for applying
     * all motion and verifies the vehicle/passenger relationship.
     */
    public static void sendVehicleInput(int inputFlags) {
        if ((inputFlags & VALID_INPUT_MASK) == 0) return;
        register();
        CHANNEL.sendToServer(new VehicleInputPacket(inputFlags & VALID_INPUT_MASK));
    }

    /**
     * Forge C2S replacement for rocket_jump/rocket_pitch/rocket_yaw and
     * lander_pitch/lander_yaw/lander_accelerate.
     *
     * Never trust arbitrary entity IDs or client-supplied positions. This packet
     * only controls the sender's current ridden vehicle and applies at most once
     * per server tick.
     */
    public record VehicleInputPacket(int inputFlags) {
        private static void encode(VehicleInputPacket packet, FriendlyByteBuf buf) {
            buf.writeVarInt(packet.inputFlags);
        }

        private static VehicleInputPacket decode(FriendlyByteBuf buf) {
            return new VehicleInputPacket(buf.readVarInt());
        }

        private static void handle(VehicleInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            ServerPlayer rider = context.getSender();
            if (rider != null) {
                context.enqueueWork(() -> applyInput(rider, packet.inputFlags));
            }
            context.setPacketHandled(true);
        }

        private static void applyInput(ServerPlayer rider, int flags) {
            if ((flags & ~VALID_INPUT_MASK) != 0 || !rider.isPassenger()) return;
            int tick = rider.server.getTickCount();
            Integer prior = LAST_INPUT_TICK.get(rider);
            if (prior != null && prior == tick) return;
            LAST_INPUT_TICK.put(rider, tick);

            Entity vehicle = rider.getVehicle();
            if (vehicle instanceof RocketEntity rocket) {
                if (rocket.getLaunchStage().ordinal() < LaunchStage.IGNITED.ordinal()) {
                    if ((flags & JUMP) != 0) rocket.onJump();
                } else if (rocket.getLaunchStage() == LaunchStage.LAUNCHED) {
                    boolean up = (flags & UP) != 0;
                    boolean down = (flags & DOWN) != 0;
                    boolean left = (flags & LEFT) != 0;
                    boolean right = (flags & RIGHT) != 0;
                    if (up != down) {
                        rocket.setXRot(Mth.clamp(rocket.getXRot() + (up ? -2.0F : 2.0F), -85.0F, 85.0F));
                    }
                    if (left != right) {
                        rocket.setYRot(Mth.wrapDegrees(rocket.getYRot() + (left ? -2.0F : 2.0F)));
                    }
                    rocket.hasImpulse = true;
                }
            } else if (vehicle instanceof LanderEntity lander && !lander.onGround()) {
                boolean up = (flags & UP) != 0;
                boolean down = (flags & DOWN) != 0;
                boolean left = (flags & LEFT) != 0;
                boolean right = (flags & RIGHT) != 0;
                if (up != down) {
                    lander.setXRot(Mth.clamp(lander.getXRot() + (up ? -1.0F : 1.0F), -45.0F, 45.0F));
                }
                if (left != right) {
                    lander.setYRot(Mth.wrapDegrees(lander.getYRot() + (left ? 1.0F : -1.0F)));
                }
                Vec3 velocity = lander.getDeltaMovement();
                if ((flags & JUMP) != 0) {
                    lander.setDeltaMovement(velocity.x, Math.min(velocity.y + 0.03D, lander.getY() < 90 ? -0.15D : -1.0D), velocity.z);
                } else if ((flags & SNEAK) != 0) {
                    lander.setDeltaMovement(velocity.x, Math.max(velocity.y - 0.022D, -1.0D), velocity.z);
                }
                lander.hasImpulse = true;
            }
        }
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
