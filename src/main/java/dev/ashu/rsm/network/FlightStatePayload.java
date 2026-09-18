package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.power.FlightMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

/**
 * Client to server: the local Bearer's current flight mode, speed (blocks/second) and unit direction.
 * Sent every tick while in fast flight and once when returning to HOVER.
 */
public record FlightStatePayload(FlightMode mode, float speed, float dx, float dy, float dz) implements CustomPacketPayload {
    public static final Type<FlightStatePayload> TYPE = new Type<>(RawSuperhumanMod.id("flight_state"));

    private static final StreamCodec<ByteBuf, FlightMode> MODE_CODEC =
        ByteBufCodecs.BYTE.map(b -> FlightMode.byOrdinal(b), mode -> (byte) mode.ordinal());

    public static final StreamCodec<ByteBuf, FlightStatePayload> STREAM_CODEC = StreamCodec.composite(
        MODE_CODEC, FlightStatePayload::mode,
        ByteBufCodecs.FLOAT, FlightStatePayload::speed,
        ByteBufCodecs.FLOAT, FlightStatePayload::dx,
        ByteBufCodecs.FLOAT, FlightStatePayload::dy,
        ByteBufCodecs.FLOAT, FlightStatePayload::dz,
        FlightStatePayload::new
    );

    public static FlightStatePayload of(FlightMode mode, double speed, Vec3 direction) {
        return new FlightStatePayload(mode, (float) speed, (float) direction.x, (float) direction.y, (float) direction.z);
    }

    public Vec3 direction() {
        return new Vec3(dx, dy, dz);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
