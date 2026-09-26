package dev.ashu.rsm.attack;

import dev.ashu.rsm.RsmConfig;

/** The super attacks of a Bearer. Numbers come from the (synced) server config. */
public enum AttackKind {
    /** Arc in front of the Bearer at sword reach: everything living in it takes the damage. */
    SLASH,
    /** Single target along the crosshair ray, heavy knockback. */
    STRIKE,
    /** Directed blast along the look: ragged block destruction, damage and knockback in a cone. */
    CLAP;

    public double damage() {
        return switch (this) {
            case SLASH -> RsmConfig.SLASH_DAMAGE.get();
            case STRIKE -> RsmConfig.STRIKE_DAMAGE.get();
            case CLAP -> RsmConfig.CLAP_DAMAGE.get();
        };
    }

    public int cooldownTicks() {
        double seconds = switch (this) {
            case SLASH -> RsmConfig.SLASH_COOLDOWN.get();
            case STRIKE -> RsmConfig.STRIKE_COOLDOWN.get();
            case CLAP -> RsmConfig.CLAP_COOLDOWN.get();
        };
        return (int) Math.round(seconds * 20.0);
    }

    public double range() {
        return switch (this) {
            case SLASH -> RsmConfig.SLASH_RANGE.get();
            case STRIKE -> RsmConfig.STRIKE_RANGE.get();
            case CLAP -> RsmConfig.CLAP_RANGE.get();
        };
    }

    public static AttackKind byOrdinal(int ordinal) {
        AttackKind[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SLASH;
    }
}
