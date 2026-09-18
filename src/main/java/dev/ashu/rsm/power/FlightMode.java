package dev.ashu.rsm.power;

/**
 * Where a Bearer is in the flight state machine. Everything except HOVER is "fast flight":
 * the Bearer moves along its look vector at a controlled Speed and is rendered in the elytra pose.
 */
public enum FlightMode {
    /** Vanilla creative-style flight; the mod does not touch movement. */
    HOVER,
    /** Sprint + forward held: speed ramps up to the configured maximum. */
    BOOST,
    /** Speed locked by pressing sneak during BOOST; keys released, steering by look. */
    CRUISE,
    /** Boost released or cruise cancelled: slowing down until hover speed, then back to HOVER. */
    DECEL;

    public boolean isFast() {
        return this != HOVER;
    }

    public static FlightMode byOrdinal(int ordinal) {
        FlightMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : HOVER;
    }
}
