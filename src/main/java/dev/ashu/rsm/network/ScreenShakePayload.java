package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: shake your camera by up to {@code degrees}, fading out over {@code ticks}. */
public record ScreenShakePayload(float degrees, int ticks) implements CustomPacketPayload {
    public static final Type<ScreenShakePayload> TYPE = new Type<>(RawSuperhumanMod.id("screen_shake"));

    public static final StreamCodec<ByteBuf, ScreenShakePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, ScreenShakePayload::degrees,
        ByteBufCodecs.VAR_INT, ScreenShakePayload::ticks,
        ScreenShakePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
