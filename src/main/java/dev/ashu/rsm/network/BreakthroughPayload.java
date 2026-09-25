package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.power.Breakthrough;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Client to server: the blocks the local Bearer just tunnelled through (already gone on the client). */
public record BreakthroughPayload(List<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<BreakthroughPayload> TYPE = new Type<>(RawSuperhumanMod.id("breakthrough"));

    public static final StreamCodec<ByteBuf, BreakthroughPayload> STREAM_CODEC = BlockPos.STREAM_CODEC
        .apply(ByteBufCodecs.list(Breakthrough.MAX_BLOCKS_PER_PACKET))
        .map(BreakthroughPayload::new, BreakthroughPayload::positions);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
