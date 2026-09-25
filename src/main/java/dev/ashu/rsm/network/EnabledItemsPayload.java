package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client on login: which ToggleableItems the server has switched off (bit = ordinal). */
public record EnabledItemsPayload(int disabledMask) implements CustomPacketPayload {
    public static final Type<EnabledItemsPayload> TYPE = new Type<>(RawSuperhumanMod.id("enabled_items"));

    public static final StreamCodec<ByteBuf, EnabledItemsPayload> STREAM_CODEC =
        ByteBufCodecs.VAR_INT.map(EnabledItemsPayload::new, EnabledItemsPayload::disabledMask);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
