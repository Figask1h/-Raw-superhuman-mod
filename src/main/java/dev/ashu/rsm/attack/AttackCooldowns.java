package dev.ashu.rsm.attack;

/** Per-player attack cooldowns, in player ticks. Transient; lives in an attachment on the server and a singleton on the client. */
public final class AttackCooldowns {
    private final int[] readyTick = new int[AttackKind.values().length];
    private final int[] startedTick = new int[AttackKind.values().length];

    public boolean isReady(AttackKind kind, int now) {
        return now >= readyTick[kind.ordinal()];
    }

    public void trigger(AttackKind kind, int now, int cooldownTicks) {
        startedTick[kind.ordinal()] = now;
        readyTick[kind.ordinal()] = now + cooldownTicks;
    }

    /** 1 right after use, 0 when ready again. */
    public float remainingFraction(AttackKind kind, int now) {
        int ready = readyTick[kind.ordinal()];
        int started = startedTick[kind.ordinal()];
        if (now >= ready || ready <= started) return 0.0F;
        return (float) (ready - now) / (float) (ready - started);
    }

    public void reset() {
        java.util.Arrays.fill(readyTick, 0);
        java.util.Arrays.fill(startedTick, 0);
    }
}
