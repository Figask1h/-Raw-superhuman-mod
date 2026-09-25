package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.power.PassiveEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: the player pressed the switch key of a Passive Effect. */
public record TogglePassivePayload(PassiveEffect effect) implements CustomPacketPayload {
    public static final Type<TogglePassivePayload> TYPE = new Type<>(RawSuperhumanMod.id("toggle_passive"));

    public static final StreamCodec<ByteBuf, TogglePassivePayload> STREAM_CODEC =
        ByteBufCodecs.BYTE.map(b -> new TogglePassivePayload(PassiveEffect.byOrdinal(b)), payload -> (byte) payload.effect().ordinal());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
