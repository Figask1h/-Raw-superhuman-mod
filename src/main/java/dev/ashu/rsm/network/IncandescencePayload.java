package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to the players watching a Bearer: its Incandescence heat, 0 (cold) to Incandescence.LEVELS. */
public record IncandescencePayload(int entityId, int heat) implements CustomPacketPayload {
    public static final Type<IncandescencePayload> TYPE = new Type<>(RawSuperhumanMod.id("incandescence"));

    public static final StreamCodec<ByteBuf, IncandescencePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, IncandescencePayload::entityId,
        ByteBufCodecs.VAR_INT, IncandescencePayload::heat,
        IncandescencePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
