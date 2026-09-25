package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: which Passive Effects the player has switched off (bit = ordinal). Sent on login and after every switch. */
public record PassiveEffectsPayload(int disabledMask) implements CustomPacketPayload {
    public static final Type<PassiveEffectsPayload> TYPE = new Type<>(RawSuperhumanMod.id("passive_effects"));

    public static final StreamCodec<ByteBuf, PassiveEffectsPayload> STREAM_CODEC =
        ByteBufCodecs.VAR_INT.map(PassiveEffectsPayload::new, PassiveEffectsPayload::disabledMask);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
