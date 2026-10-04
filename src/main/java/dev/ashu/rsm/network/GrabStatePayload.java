package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: entity {@code holderId} now holds {@code captiveId}, or nobody when it is -1. */
public record GrabStatePayload(int holderId, int captiveId) implements CustomPacketPayload {
    public static final Type<GrabStatePayload> TYPE = new Type<>(RawSuperhumanMod.id("grab_state"));

    public static final StreamCodec<ByteBuf, GrabStatePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, GrabStatePayload::holderId,
        ByteBufCodecs.VAR_INT, GrabStatePayload::captiveId,
        GrabStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
