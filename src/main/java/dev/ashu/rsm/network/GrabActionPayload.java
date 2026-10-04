package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: a Grab key was used. {@code targetId} is the entity under the crosshair for GRAB, else unused. */
public record GrabActionPayload(Action action, int targetId) implements CustomPacketPayload {
    public static final Type<GrabActionPayload> TYPE = new Type<>(RawSuperhumanMod.id("grab_action"));

    public enum Action {
        GRAB, THROW, RELEASE, STRUGGLE;

        static Action byOrdinal(int ordinal) {
            Action[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : RELEASE;
        }
    }

    public static final StreamCodec<ByteBuf, GrabActionPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE.map(b -> Action.byOrdinal(b), action -> (byte) action.ordinal()), GrabActionPayload::action,
        ByteBufCodecs.VAR_INT, GrabActionPayload::targetId,
        GrabActionPayload::new
    );

    public static GrabActionPayload of(Action action) {
        return new GrabActionPayload(action, -1);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
