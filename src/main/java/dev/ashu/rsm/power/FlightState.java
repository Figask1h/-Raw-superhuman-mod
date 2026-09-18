package dev.ashu.rsm.power;

import net.minecraft.world.phys.Vec3;

/**
 * Server-side view of a flying Bearer, fed by {@link dev.ashu.rsm.network.FlightStatePayload}.
 * Transient: rebuilt from client packets, never saved.
 */
public final class FlightState {
    public FlightMode mode = FlightMode.HOVER;
    /** Blocks per second. */
    public double speed;
    public Vec3 direction = Vec3.ZERO;
    /** Player tick count when the last state packet arrived; used to expire stale fast flight. */
    public int lastUpdateTick;
    /**
     * Ascent (Ad Astra) is armed below the atmosphere and fires once when crossing the leave height;
     * it re-arms only after descending well below it, so closing the planet screen does not reopen it.
     */
    public boolean ascentArmed = true;

    public boolean isFast() {
        return mode.isFast();
    }

    public void reset() {
        mode = FlightMode.HOVER;
        speed = 0.0;
        direction = Vec3.ZERO;
    }
}
