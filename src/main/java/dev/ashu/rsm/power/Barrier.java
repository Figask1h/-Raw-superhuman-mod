package dev.ashu.rsm.power;

import dev.ashu.rsm.RsmConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Barrier: the moment a Bearer's Speed reaches the Breakthrough threshold. Marked by a burst like a breeze's
 * wind charge, which everyone nearby sees and hears; it pushes nothing. The next burst needs Speed to drop
 * {@link #REARM_MARGIN} below the threshold first, so hovering around it does not fire every tick.
 */
public final class Barrier {
    /** Blocks/second below the Barrier that Speed must fall to before the next burst. */
    public static final double REARM_MARGIN = 10.0;

    /** The Barrier speed, blocks/second: the same line Breakthrough starts at. */
    public static double speed() {
        return RsmConfig.BREAKTHROUGH_MIN_SPEED.get();
    }

    /** Called with every fast-flight report. True when this report crossed the Barrier and the burst fired. */
    public static boolean track(ServerPlayer player, FlightState state) {
        double barrier = speed();
        if (state.speed < barrier - REARM_MARGIN) {
            state.barrierArmed = true;
            return false;
        }
        if (!state.barrierArmed || state.speed < barrier) return false;
        state.barrierArmed = false;
        burst(player);
        return true;
    }

    /** What a breeze wind charge shows and plays when it explodes, without the explosion. */
    private static void burst(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 at = player.position().add(0.0, player.getBbHeight() / 2.0, 0.0);
        level.sendParticles(ParticleTypes.GUST_EMITTER_LARGE, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        float pitch = (1.0F + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F) * 0.7F;
        level.playSound(null, at.x, at.y, at.z, SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 4.0F, pitch);
    }

    private Barrier() {}
}
