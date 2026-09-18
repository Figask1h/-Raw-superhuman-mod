package dev.ashu.rsm.client;

import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.power.Bearer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Dash: a burst of movement in the direction the player is moving (WASD, plus jump/sneak while
 * hovering; standing still dashes forward). On foot or in hover it is a velocity impulse that
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
            Vec3 impulse = movementDirection(player).scale(RsmConfig.DASH_DISTANCE.get() * IMPULSE_PER_BLOCK);
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

    /** Movement keys relative to the camera yaw; vertical keys count only while flying. Falls back to velocity, then to facing. */
    private static Vec3 movementDirection(LocalPlayer player) {
        Input input = player.input;
        double forward = input.forwardImpulse;
        double left = input.leftImpulse;
        double up = 0.0;
        if (player.getAbilities().flying) {
            if (input.jumping) up += 1.0;
            if (input.shiftKeyDown) up -= 1.0;
        }
        if (forward * forward + left * left + up * up > 1.0E-4) {
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            float sin = Mth.sin(yaw);
            float cos = Mth.cos(yaw);
            return new Vec3(left * cos - forward * sin, up, forward * cos + left * sin).normalize();
        }
        Vec3 velocity = player.getDeltaMovement();
        Vec3 horizontal = new Vec3(velocity.x, 0.0, velocity.z);
        if (horizontal.lengthSqr() > 1.0E-3) return horizontal.normalize();
        return Vec3.directionFromRotation(0.0F, player.getYRot());
    }

    private Dash() {}
}
