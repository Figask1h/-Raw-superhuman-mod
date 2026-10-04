package dev.ashu.rsm.client;

import net.minecraft.Util;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

/**
 * The Clap's arm animation: both arms swing up in front of the chest already apart, snap together, hold, and
 * drop. Shown on the clapping player in third person (HumanoidModelMixin) and, for the local player, in first
 * person ({@link FirstPersonClap}). The local player starts it on the key press; others get a ClapPayload.
 *
 * <p>Timed in real milliseconds and keyed by entity id, so it needs no client-only types: the payload handler
 * that calls {@link #start} is registered by common code.
 */
public final class ClapAnimation {
    private static final long DURATION_MS = 450;
    /** Progress at which the arms are fully up. */
    private static final float RAISED_AT = 0.2F;
    /** Progress at which the hands meet: about 110 ms, roughly when the blast shows up. */
    private static final float CLAP_AT = 0.25F;
    /** Progress from which the arms drop back. */
    private static final float LOWER_FROM = 0.65F;
    /** Arm yaw towards the body's middle line, radians: apart at the start, hands touching at the clap. */
    private static final float APART = -0.6F;
    private static final float TOGETHER = 0.38F;

    private static final Map<Integer, Long> STARTED = new HashMap<>();

    public static void start(int entityId) {
        STARTED.put(entityId, Util.getMillis());
    }

    /** How far this entity's clap has got, 0 to 1, or -1 when it is not clapping. */
    public static float progress(int entityId) {
        if (STARTED.isEmpty()) return -1.0F;
        Long started = STARTED.get(entityId);
        if (started == null) return -1.0F;
        float progress = (Util.getMillis() - started) / (float) DURATION_MS;
        if (progress < 1.0F) return progress;
        STARTED.remove(entityId);
        return -1.0F;
    }

    /** How much the clap pose overrides the normal arm pose: rises to 1, holds, falls back to 0. */
    public static float weight(float progress) {
        if (progress < RAISED_AT) return ease(progress / RAISED_AT);
        if (progress > LOWER_FROM) return ease((1.0F - progress) / (1.0F - LOWER_FROM));
        return 1.0F;
    }

    /** Each arm's yaw towards the middle line, radians. Speeds up into the clap, like real hands. */
    public static float inward(float progress) {
        float t = Mth.clamp(progress / CLAP_AT, 0.0F, 1.0F);
        return Mth.lerp(t * t, APART, TOGETHER);
    }

    private static float ease(float t) {
        return (float) Mth.smoothstep(Mth.clamp(t, 0.0F, 1.0F));
    }

    static void reset() {
        STARTED.clear();
    }

    private ClapAnimation() {}
}
