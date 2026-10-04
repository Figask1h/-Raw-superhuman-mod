package dev.ashu.rsm.attack;

import dev.ashu.rsm.network.ScreenShakePayload;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * What Strike and Clap look like beyond the hit itself: a trail of rings like the warden's sonic boom along the
 * blow, a flash where it lands, and a camera shake for the attacker and for every player it hits. Everyone
 * nearby sees the particles; only the players involved shake.
 */
final class AttackEffects {
    /** The first ring sits this far ahead of the eyes, so it does not fill the attacker's own view. */
    private static final double TRAIL_START = 1.5;
    /** One ring per block, as the warden does. */
    private static final double TRAIL_STEP = 1.0;

    static void sonicTrail(ServerLevel level, Vec3 from, Vec3 direction, double length) {
        for (double d = TRAIL_START; d <= length; d += TRAIL_STEP) {
            Vec3 at = from.add(direction.scale(d));
            level.sendParticles(ParticleTypes.SONIC_BOOM, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    static void flash(ServerLevel level, Vec3 at) {
        level.sendParticles(ParticleTypes.FLASH, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    static void shakeAttacker(ServerPlayer attacker, AttackKind kind) {
        switch (kind) {
            case STRIKE -> shake(attacker, 1.5F, 6);
            case CLAP -> shake(attacker, 2.5F, 10);
            case SLASH -> {}
        }
    }

    /** Only players have a camera; anything else hit is left alone. */
    static void shakeHit(LivingEntity target, AttackKind kind) {
        switch (kind) {
            case STRIKE -> shake(target, 4.0F, 12);
            case CLAP -> shake(target, 3.0F, 12);
            case SLASH -> {}
        }
    }

    private static void shake(LivingEntity entity, float degrees, int ticks) {
        // No connection for fake players (and in GameTests).
        if (entity instanceof ServerPlayer player && player.connection != null) {
            PacketDistributor.sendToPlayer(player, new ScreenShakePayload(degrees, ticks));
        }
    }

    private AttackEffects() {}
}
