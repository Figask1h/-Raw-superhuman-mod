package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to the players watching a Captive: it just struggled, shake it. */
public record StrugglePayload(int captiveId) implements CustomPacketPayload {
    public static final Type<StrugglePayload> TYPE = new Type<>(RawSuperhumanMod.id("struggle"));

    public static final StreamCodec<ByteBuf, StrugglePayload> STREAM_CODEC =
        ByteBufCodecs.VAR_INT.map(StrugglePayload::new, StrugglePayload::captiveId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
