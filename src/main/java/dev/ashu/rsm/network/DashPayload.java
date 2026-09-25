package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: the local Bearer just dashed, so everyone else should see and hear it. */
public record DashPayload() implements CustomPacketPayload {
    public static final DashPayload INSTANCE = new DashPayload();
    public static final Type<DashPayload> TYPE = new Type<>(RawSuperhumanMod.id("dash"));

    public static final StreamCodec<ByteBuf, DashPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
