package dev.ashu.rsm.client;

import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.IntPredicate;

/**
 * How hot each visible Bearer is drawn, by entity id. Heats up as fast as the target rises, cools off at a fixed
 * rate, so the glow fades over about two seconds after slowing down.
 *
 * <p>Plain state only: the payload handler that calls {@link #setTarget} is registered by common code, so keep
 * this class free of client-only types.
 */
public final class IncandescenceGlow {
    /** Ticks to cool from full heat to cold. */
    private static final float COOLING_TICKS = 40.0F;

    private static final Map<Integer, Heat> HEAT = new HashMap<>();

    private static final class Heat {
        float previous;
        float current;
        float target;
    }

    /** Target heat 0 to 1: from the server for others, from the local flight for ourselves. */
    public static void setTarget(int entityId, float target) {
        Heat heat = HEAT.get(entityId);
        if (heat == null) {
            if (target <= 0.0F) return;
            heat = new Heat();
            HEAT.put(entityId, heat);
        }
        heat.target = target;
    }

    /** Once per client tick. Forgets entities that are gone or have cooled down completely. */
    static void tick(IntPredicate exists) {
        Iterator<Map.Entry<Integer, Heat>> entries = HEAT.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Integer, Heat> entry = entries.next();
            Heat heat = entry.getValue();
            heat.previous = heat.current;
            heat.current = heat.target >= heat.current ? heat.target : Math.max(heat.target, heat.current - 1.0F / COOLING_TICKS);
            boolean cold = heat.current <= 0.0F && heat.previous <= 0.0F;
            if (cold || !exists.test(entry.getKey())) entries.remove();
        }
    }

    /** Heat to draw this frame, 0 to 1. */
    static float heat(int entityId, float partialTick) {
        Heat heat = HEAT.get(entityId);
        return heat == null ? 0.0F : Mth.lerp(partialTick, heat.previous, heat.current);
    }

    static void reset() {
        HEAT.clear();
    }

    private IncandescenceGlow() {}
}
