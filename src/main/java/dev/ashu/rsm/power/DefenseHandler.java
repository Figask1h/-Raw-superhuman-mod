package dev.ashu.rsm.power;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Damage model of a Bearer. Physical damage (tag rsm:physical, editable by datapacks) is
 * multiplied by the configured fraction after armor; everything else is nullified before it can
 * hurt, knock back or play a hurt sound. Damage that bypasses invulnerability (kill command, the
 * void) is never touched. Harmful effects still apply; only their damage is gone.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
public final class DefenseHandler {
    public static final TagKey<DamageType> PHYSICAL = TagKey.create(Registries.DAMAGE_TYPE, RawSuperhumanMod.id("physical"));

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!Bearer.isBearer(event.getEntity())) return;
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (!source.is(PHYSICAL)) {
            event.setCanceled(true);
        }
    }

    /** Runs after armor, enchantments and absorption: the 93% applies to what would actually be taken. */
    @SubscribeEvent
    static void onDamagePre(LivingDamageEvent.Pre event) {
        if (!Bearer.isBearer(event.getEntity())) return;
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (source.is(PHYSICAL)) {
            event.setNewDamage((float) (event.getNewDamage() * RsmConfig.PHYSICAL_DAMAGE_MULTIPLIER.get()));
        }
    }

    private DefenseHandler() {}
}
