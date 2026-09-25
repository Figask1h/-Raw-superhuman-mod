package dev.ashu.rsm.power;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * The Passive Effects the Ring grants. Each is refreshed shortly before it runs out, so it never flickers,
 * and the ring's own instance is recognisable (ambient, no particles) so switching it off never strips
 * a potion or a beacon effect.
 */
public enum PassiveEffect implements StringRepresentable {
    HASTE("haste", MobEffects.DIG_SPEED, 2, 60, 20),
    /** Vanilla starts flashing night vision below 200 ticks left, so keep it comfortably above that. */
    NIGHT_VISION("night_vision", MobEffects.NIGHT_VISION, 0, 400, 240);

    public static final Codec<PassiveEffect> CODEC = StringRepresentable.fromEnum(PassiveEffect::values);

    private final String name;
    private final Holder<MobEffect> effect;
    private final int amplifier;
    private final int durationTicks;
    private final int refreshTicks;

    PassiveEffect(String name, Holder<MobEffect> effect, int amplifier, int durationTicks, int refreshTicks) {
        this.name = name;
        this.effect = effect;
        this.amplifier = amplifier;
        this.durationTicks = durationTicks;
        this.refreshTicks = refreshTicks;
    }

    public Holder<MobEffect> effect() {
        return effect;
    }

    /** Called every tick while the ring is worn and this effect is switched on. */
    public void apply(LivingEntity wearer) {
        MobEffectInstance current = wearer.getEffect(effect);
        if (current == null || current.getAmplifier() < amplifier || current.getDuration() <= refreshTicks) {
            wearer.addEffect(new MobEffectInstance(effect, durationTicks, amplifier, true, false, true));
        }
    }

    /** Takes away the ring's own instance of the effect, leaving potions and beacons alone. */
    public void remove(LivingEntity wearer) {
        MobEffectInstance current = wearer.getEffect(effect);
        if (current != null && current.isAmbient() && !current.isVisible()) {
            wearer.removeEffect(effect);
        }
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static PassiveEffect byOrdinal(int ordinal) {
        PassiveEffect[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : HASTE;
    }
}
