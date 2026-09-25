package dev.ashu.rsm.client;

import dev.ashu.rsm.power.PassiveEffect;
import dev.ashu.rsm.power.PassiveEffects;

/**
 * The local player's Passive Effect switches as last told by the server, for the HUD. Flipped optimistically
 * on key press; the server's answer overwrites it. Outlives respawns, cleared on logout.
 */
public final class ClientPassives {
    private static final PassiveEffects SWITCHES = new PassiveEffects();

    public static boolean isEnabled(PassiveEffect effect) {
        return SWITCHES.isEnabled(effect);
    }

    static void toggle(PassiveEffect effect) {
        SWITCHES.toggle(effect);
    }

    /** Called by the payload handler, which common code registers: keep this class free of client-only types. */
    public static void set(int disabledMask) {
        SWITCHES.setDisabledMask(disabledMask);
    }

    static void reset() {
        SWITCHES.setDisabledMask(0);
    }

    private ClientPassives() {}
}
