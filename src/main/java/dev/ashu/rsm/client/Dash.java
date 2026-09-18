package dev.ashu.rsm.client;

import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.power.Bearer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Dash: a burst of movement along the camera. On foot or in hover it is a velocity impulse that
 * vanilla friction turns into roughly dashDistance blocks; in fast flight it becomes a speed boost.
 * Movement is client-authoritative, so the whole thing lives on the client; only the cooldown is tracked.
 */
public final class Dash {
    /** Ticks between dashes; not an AttackKind because the server never sees a dash. */
    private static int readyTick;
    private static int startedTick;
    /**
     * Airborne horizontal velocity decays by 0.91 per tick, so about six ticks of travel cover
     * v0 * 4.7 blocks; the impulse is scaled so that distance matches dashDistance.
     */
    private static final double IMPULSE_PER_BLOCK = 1.0 / 4.7;
    private static final double GROUND_LIFT = 0.12;

    public static boolean isReady(int now) {
        return now >= readyTick;
    }

    /** 1 right after use, 0 when ready again. */
    public static float remainingFraction(int now) {
        if (now >= readyTick || readyTick <= startedTick) return 0.0F;
        return (float) (readyTick - now) / (float) (readyTick - startedTick);
    }

    public static void reset() {
        readyTick = 0;
        startedTick = 0;
    }

    static void tryDash(LocalPlayer player) {
        if (player.isSpectator() || player.isPassenger() || player.isSleeping() || !Bearer.isBearer(player)) return;
        int now = player.tickCount;
        if (!isReady(now)) return;
        startedTick = now;
        readyTick = now + (int) Math.round(RsmConfig.DASH_COOLDOWN.get() * 20.0);

        FlightController controller = FlightController.get();
        if (controller.isFast()) {
            controller.dashInFlight(player);
        } else {
            Vec3 look = player.getLookAngle();
            if (player.onGround() && look.y < 0.0) {
                // Do not dash into the floor: flatten the direction when standing.
                look = new Vec3(look.x, 0.0, look.z);
                if (look.lengthSqr() < 1.0E-6) look = Vec3.directionFromRotation(0.0F, player.getYRot());
                look = look.normalize();
            }
            Vec3 impulse = look.scale(RsmConfig.DASH_DISTANCE.get() * IMPULSE_PER_BLOCK);
            double lift = player.onGround() ? GROUND_LIFT : 0.0;
            player.setDeltaMovement(impulse.x, Mth.clamp(impulse.y + lift, -3.0, 3.0), impulse.z);
        }

        player.playSound(SoundEvents.WIND_CHARGE_BURST.value(), 0.8F, 1.3F);
        for (int i = 0; i < 12; i++) {
            player.level().addParticle(ParticleTypes.CLOUD,
                player.getRandomX(0.6), player.getY() + player.getRandom().nextDouble() * 0.6, player.getRandomZ(0.6),
                (player.getRandom().nextDouble() - 0.5) * 0.2, 0.02, (player.getRandom().nextDouble() - 0.5) * 0.2);
        }
    }

    private Dash() {}
}
