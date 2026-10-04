package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: drop out of fast flight and take this velocity (blocks per tick). A counterattack's knockback. */
public record PushPayload(float x, float y, float z) implements CustomPacketPayload {
    public static final Type<PushPayload> TYPE = new Type<>(RawSuperhumanMod.id("push"));

    public static final StreamCodec<ByteBuf, PushPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, PushPayload::x,
        ByteBufCodecs.FLOAT, PushPayload::y,
        ByteBufCodecs.FLOAT, PushPayload::z,
        PushPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
