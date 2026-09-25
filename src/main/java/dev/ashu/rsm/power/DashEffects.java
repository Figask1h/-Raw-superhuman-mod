package dev.ashu.rsm.power;

import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Server side of Dash. The dash itself and the dasher's own sound and particles happen on the client;
 * the server only repeats them for everyone else, so other players see a Bearer dash too.
 */
public final class DashEffects {
    private static final int PARTICLES = 12;

    public static void showToOthers(ServerPlayer player) {
        if (!Bearer.isBearer(player) || player.isSpectator() || !player.isAlive()) return;
        FlightState state = player.getData(ModAttachments.FLIGHT);
        // A dash cannot come faster than its cooldown; half of it leaves room for lag and still stops packet spam.
        int minInterval = (int) Math.round(RsmConfig.DASH_COOLDOWN.get() * 10.0);
        if (player.tickCount - state.lastDashTick < minInterval) return;
        state.lastDashTick = player.tickCount;

        ServerLevel level = player.serverLevel();
        double x = player.getX();
        double y = player.getY() + 0.3;
        double z = player.getZ();
        level.playSound(player, x, y, z, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 0.8F, 1.3F);
        for (ServerPlayer viewer : level.players()) {
            if (viewer != player) {
                level.sendParticles(viewer, ParticleTypes.CLOUD, false, x, y, z, PARTICLES, 0.3, 0.3, 0.3, 0.05);
            }
        }
    }

    private DashEffects() {}
}
