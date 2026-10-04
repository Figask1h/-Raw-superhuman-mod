package dev.ashu.rsm.client;

import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Which Holder holds which Captive, as last told by the server, plus recent struggles and a pending push. Read by
 * {@link GrabControls} to keep Captives in place on screen and by the HUD and models.
 *
 * <p>Plain state only: the payload handlers that fill it are registered by common code, so keep this class free of
 * client-only types.
 */
public final class ClientGrabs {
    /** How long a struggle shakes the Captive's model. */
    private static final long STRUGGLE_SHAKE_MS = 250;

    private static final Map<Integer, Integer> CAPTIVE_BY_HOLDER = new HashMap<>();
    private static final Map<Integer, Integer> HOLDER_BY_CAPTIVE = new HashMap<>();
    private static final Map<Integer, Long> STRUGGLED = new HashMap<>();
    @Nullable
    private static float[] pendingPush;

    /** {@code captiveId} -1: the Holder holds nobody now. */
    public static void set(int holderId, int captiveId) {
        Integer previous = CAPTIVE_BY_HOLDER.remove(holderId);
        if (previous != null) HOLDER_BY_CAPTIVE.remove(previous);
        if (captiveId < 0) return;
        CAPTIVE_BY_HOLDER.put(holderId, captiveId);
        HOLDER_BY_CAPTIVE.put(captiveId, holderId);
    }

    public static boolean isHolding(int entityId) {
        return CAPTIVE_BY_HOLDER.containsKey(entityId);
    }

    public static boolean isCaptive(int entityId) {
        return HOLDER_BY_CAPTIVE.containsKey(entityId);
    }

    @Nullable
    static Integer captiveOf(int holderId) {
        return CAPTIVE_BY_HOLDER.get(holderId);
    }

    static Map<Integer, Integer> holders() {
        return HOLDER_BY_CAPTIVE;
    }

    public static void struggled(int captiveId) {
        STRUGGLED.put(captiveId, Util.getMillis());
    }

    /** 1 right after a struggle, fading to 0. */
    static float struggleShake(int captiveId) {
        Long at = STRUGGLED.get(captiveId);
        if (at == null) return 0.0F;
        long age = Util.getMillis() - at;
        if (age < STRUGGLE_SHAKE_MS) return 1.0F - age / (float) STRUGGLE_SHAKE_MS;
        STRUGGLED.remove(captiveId);
        return 0.0F;
    }

    /** A counterattack's knockback for the local player, applied on the next client tick. */
    public static void push(float x, float y, float z) {
        pendingPush = new float[] {x, y, z};
    }

    @Nullable
    static float[] takePush() {
        float[] push = pendingPush;
        pendingPush = null;
        return push;
    }

    static void reset() {
        CAPTIVE_BY_HOLDER.clear();
        HOLDER_BY_CAPTIVE.clear();
        STRUGGLED.clear();
        pendingPush = null;
    }

    private ClientGrabs() {}
}
