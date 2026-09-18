package dev.ashu.rsm.attack;

import dev.ashu.rsm.RsmConfig;

/** The two super attacks of a Bearer. Numbers come from the (synced) server config. */
public enum AttackKind {
    /** Arc in front of the Bearer at sword reach: everything living in it takes the damage. */
    SLASH,
    /** Single target along the crosshair ray, heavy knockback. */
    STRIKE;

    public double damage() {
        return this == SLASH ? RsmConfig.SLASH_DAMAGE.get() : RsmConfig.STRIKE_DAMAGE.get();
    }

    public int cooldownTicks() {
        double seconds = this == SLASH ? RsmConfig.SLASH_COOLDOWN.get() : RsmConfig.STRIKE_COOLDOWN.get();
        return (int) Math.round(seconds * 20.0);
    }

    public double range() {
        return this == SLASH ? RsmConfig.SLASH_RANGE.get() : RsmConfig.STRIKE_RANGE.get();
    }

    public static AttackKind byOrdinal(int ordinal) {
        AttackKind[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SLASH;
    }
}
