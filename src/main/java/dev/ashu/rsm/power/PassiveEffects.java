package dev.ashu.rsm.power;

import com.mojang.serialization.Codec;

import java.util.EnumSet;
import java.util.List;

/**
 * Which Passive Effects a player has switched off. Lives in an attachment that is saved with the player
 * and kept through death; the client gets a copy for the HUD.
 */
public final class PassiveEffects {
    public static final Codec<PassiveEffects> CODEC = PassiveEffect.CODEC.listOf()
        .xmap(PassiveEffects::new, effects -> List.copyOf(effects.disabled));

    private final EnumSet<PassiveEffect> disabled = EnumSet.noneOf(PassiveEffect.class);

    public PassiveEffects() {}

    private PassiveEffects(List<PassiveEffect> disabled) {
        this.disabled.addAll(disabled);
    }

    public boolean isEnabled(PassiveEffect effect) {
        return !disabled.contains(effect);
    }

    public void toggle(PassiveEffect effect) {
        if (!disabled.remove(effect)) disabled.add(effect);
    }

    /** Switched-off effects as a bit mask (bit = ordinal), for the network. */
    public int disabledMask() {
        int mask = 0;
        for (PassiveEffect effect : disabled) mask |= 1 << effect.ordinal();
        return mask;
    }

    public void setDisabledMask(int mask) {
        disabled.clear();
        for (PassiveEffect effect : PassiveEffect.values()) {
            if ((mask & (1 << effect.ordinal())) != 0) disabled.add(effect);
        }
    }
}
