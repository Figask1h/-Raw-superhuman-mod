package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to the players tracking a Bearer: that entity just clapped, play the clap animation on it. */
public record ClapPayload(int entityId) implements CustomPacketPayload {
    public static final Type<ClapPayload> TYPE = new Type<>(RawSuperhumanMod.id("clap"));

    public static final StreamCodec<ByteBuf, ClapPayload> STREAM_CODEC =
        ByteBufCodecs.VAR_INT.map(ClapPayload::new, ClapPayload::entityId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
