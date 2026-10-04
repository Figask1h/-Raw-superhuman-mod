package dev.ashu.rsm.client;

/**
 * The local camera shake, started by the server through ScreenShakePayload and applied to the camera angles in
 * {@link RsmClient}. A stronger shake replaces a weaker one still running; it fades out linearly.
 *
 * <p>Plain state only: the payload handler that calls {@link #start} is registered by common code, so keep this
 * class free of client-only types.
 */
public final class ScreenShake {
    private static float degrees;
    private static int durationTicks;
    private static int remainingTicks;

    public static void start(float strengthDegrees, int ticks) {
        if (ticks <= 0 || strengthDegrees < amplitude(0.0F)) return;
        degrees = strengthDegrees;
        durationTicks = remainingTicks = ticks;
    }

    /** Current shake in degrees. */
    static float amplitude(float partialTick) {
        if (remainingTicks <= 0) return 0.0F;
        return degrees * Math.max(0.0F, remainingTicks - partialTick) / durationTicks;
    }

    static void tick() {
        if (remainingTicks > 0) remainingTicks--;
    }

    static void reset() {
        remainingTicks = 0;
    }

    private ScreenShake() {}
}
