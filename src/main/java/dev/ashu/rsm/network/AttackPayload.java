package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.attack.AttackKind;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: the local Bearer pressed an attack key. */
public record AttackPayload(AttackKind kind) implements CustomPacketPayload {
    public static final Type<AttackPayload> TYPE = new Type<>(RawSuperhumanMod.id("attack"));

    public static final StreamCodec<ByteBuf, AttackPayload> STREAM_CODEC =
        ByteBufCodecs.BYTE.map(b -> new AttackPayload(AttackKind.byOrdinal(b)), payload -> (byte) payload.kind().ordinal());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
