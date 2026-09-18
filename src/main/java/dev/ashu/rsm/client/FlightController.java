package dev.ashu.rsm.client;

import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.network.FlightStatePayload;
import dev.ashu.rsm.power.Bearer;
import dev.ashu.rsm.power.FlightHooks;
import dev.ashu.rsm.power.FlightMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Flight state machine of the local Bearer. Runs on the client (player movement is client-authoritative
 * in Minecraft) and reports its state to the server every tick while in fast flight.
 *
 * <p>Speeds are in blocks per second; velocity handed to the entity is blocks per tick.
 */
public final class FlightController {
    /** Steady-state speed of vanilla creative flight (0.05 acceleration against 0.91 friction), b/s. */
    public static final double HOVER_SPEED = 11.0;

    private static final FlightController INSTANCE = new FlightController();

    private FlightMode mode = FlightMode.HOVER;
    private double speed;
    private double cruiseSpeed;
    private Vec3 direction = Vec3.ZERO;
    private boolean sneakWasDown;
    private boolean hoverReported = true;

    public static FlightController get() {
        return INSTANCE;
    }

    public FlightMode mode() {
        return mode;
    }

    public double speed() {
        return speed;
    }

    public boolean isFast() {
        return mode.isFast();
    }

    /** 0 at hover speed, 1 at max speed. */
    public double speedFraction() {
        double max = RsmConfig.MAX_SPEED.get();
        if (max <= HOVER_SPEED) return isFast() ? 1.0 : 0.0;
        return Mth.clamp((speed - HOVER_SPEED) / (max - HOVER_SPEED), 0.0, 1.0);
    }

    public void reset() {
        mode = FlightMode.HOVER;
        speed = 0.0;
        cruiseSpeed = 0.0;
        direction = Vec3.ZERO;
        sneakWasDown = false;
        hoverReported = true;
    }

    // ------------------------------------------------------------------ input (before travel)

    /** Called from MovementInputUpdateEvent: inside aiStep, after the keyboard was read, before travel. */
    public void onMovementInput(LocalPlayer player, Input input) {
        boolean sneakDown = input.shiftKeyDown;
        boolean sneakPressed = sneakDown && !sneakWasDown;
        sneakWasDown = sneakDown;

        boolean canFly = player.getAbilities().flying && !player.isPassenger() && !player.isSpectator()
            && !player.isSleeping() && Bearer.isBearer(player);
        if (!canFly) {
            if (isFast()) toHover(player);
            return;
        }

        boolean boosting = input.up && Minecraft.getInstance().options.keySprint.isDown();
        double max = RsmConfig.MAX_SPEED.get();
        double accelPerTick = max / (RsmConfig.ACCELERATION_TIME.get() * 20.0);
        double decelPerTick = max / Math.max(0.05, RsmConfig.DECELERATION_TIME.get() * 20.0);

        switch (mode) {
            case HOVER -> {
                if (!boosting) return;
                mode = FlightMode.BOOST;
                direction = player.getLookAngle();
                speed = Math.max(HOVER_SPEED, player.getDeltaMovement().length() * 20.0);
            }
            case BOOST -> {
                if (sneakPressed) {
                    mode = FlightMode.CRUISE;
                    cruiseSpeed = speed;
                } else if (!boosting) {
                    mode = FlightMode.DECEL;
                } else {
                    speed = Math.min(max, speed + accelPerTick);
                }
            }
            case CRUISE -> {
                if (sneakPressed) {
                    mode = FlightMode.DECEL;
                } else if (boosting) {
                    speed = Math.min(max, speed + accelPerTick);
                    cruiseSpeed = speed;
                } else if (speed < cruiseSpeed) {
                    speed = Math.min(cruiseSpeed, speed + accelPerTick);
                }
            }
            case DECEL -> {
                if (boosting) {
                    mode = FlightMode.BOOST;
                } else {
                    speed -= decelPerTick;
                    if (speed <= HOVER_SPEED) {
                        player.setDeltaMovement(direction.scale(HOVER_SPEED / 20.0));
                        toHover(player);
                        return;
                    }
                }
            }
        }

        steer(player.getLookAngle());

        // Fast flight is steered by the camera only: swallow WASD, jump and sneak before vanilla sees them.
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = input.down = input.left = input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;

        hoverReported = false;
        PacketDistributor.sendToServer(FlightStatePayload.of(mode, speed, direction));
    }

    /**
     * Rotate the travel direction towards the look vector at a limited rate. Gentle steering is free;
     * only turns sharper than turnLossMinAngle bleed speed, per degree actually turned that tick.
     */
    private void steer(Vec3 look) {
        if (look.lengthSqr() < 1.0E-6) return;
        look = look.normalize();
        if (direction.lengthSqr() < 1.0E-6) {
            direction = look;
            return;
        }
        double maxTurn = Math.toRadians(RsmConfig.TURN_RATE.get());
        double angle = Math.acos(Mth.clamp(direction.dot(look), -1.0, 1.0));
        double turned = Math.min(angle, maxTurn);
        direction = rotateTowards(direction, look, turned);
        if (Math.toDegrees(angle) >= RsmConfig.TURN_LOSS_MIN_ANGLE.get()) {
            speed = Math.max(0.0, speed * (1.0 - Math.toDegrees(turned) * RsmConfig.TURN_SPEED_LOSS.get()));
        }
    }

    private static Vec3 rotateTowards(Vec3 from, Vec3 to, double radians) {
        if (radians <= 0.0) return from;
        double dot = Mth.clamp(from.dot(to), -1.0, 1.0);
        if (Math.acos(dot) <= radians + 1.0E-6) return to;
        Vec3 axis = from.cross(to);
        if (axis.lengthSqr() < 1.0E-8) {
            // Anti-parallel: any perpendicular axis will do.
            axis = Math.abs(from.y) < 0.9 ? from.cross(new Vec3(0.0, 1.0, 0.0)) : from.cross(new Vec3(1.0, 0.0, 0.0));
        }
        axis = axis.normalize();
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        // Rodrigues rotation of "from" around "axis".
        return from.scale(cos)
            .add(axis.cross(from).scale(sin))
            .add(axis.scale(axis.dot(from) * (1.0 - cos)))
            .normalize();
    }

    // ------------------------------------------------------------------ travel (replaces vanilla)

    /** Called from the Player.travel mixin for the local player. Returns true when vanilla travel must be skipped. */
    public boolean travel(LocalPlayer player) {
        if (!isFast()) return false;
        Vec3 velocity = direction.scale(speed / 20.0);
        player.setDeltaMovement(velocity);
        player.move(MoverType.SELF, velocity);
        player.resetFallDistance();
        if (player.horizontalCollision || player.verticalCollision) {
            onBlocked(player);
        }
        return true;
    }

    /** Hit a block we could not pass: rebound when fast enough, otherwise just drop to hover. */
    private void onBlocked(LocalPlayer player) {
        if (speed >= RsmConfig.REBOUND_THRESHOLD.get()) {
            // Hand vanilla a velocity that decays (0.91/tick horizontal, 0.6/tick vertical) over about reboundDistance blocks.
            double distance = RsmConfig.REBOUND_DISTANCE.get();
            Vec3 back = direction.scale(-1.0);
            player.setDeltaMovement(back.x * distance * 0.09, back.y * distance * 0.4, back.z * distance * 0.09);
        } else {
            player.setDeltaMovement(Vec3.ZERO);
        }
        toHover(player);
    }

    /** Dash while in fast flight: a burst of speed along the current travel direction (cruise remembers it). */
    public void dashInFlight(LocalPlayer player) {
        speed = Math.min(RsmConfig.MAX_SPEED.get(), speed + RsmConfig.DASH_FLIGHT_BOOST.get());
        if (mode == FlightMode.CRUISE) cruiseSpeed = speed;
        if (mode == FlightMode.DECEL) mode = FlightMode.BOOST;
    }

    // ------------------------------------------------------------------ after tick

    /** Called at the end of every client tick: keeps pose and fall-flying flag matching the mode. */
    public void tickPost(LocalPlayer player) {
        if (isFast()) {
            player.setForcedPose(Pose.FALL_FLYING);
            FlightHooks.setFallFlying(player, true);
        } else if (player.getForcedPose() == Pose.FALL_FLYING) {
            player.setForcedPose(null);
        }
    }

    private void toHover(LocalPlayer player) {
        mode = FlightMode.HOVER;
        speed = 0.0;
        cruiseSpeed = 0.0;
        if (!hoverReported) {
            hoverReported = true;
            PacketDistributor.sendToServer(FlightStatePayload.of(FlightMode.HOVER, 0.0, Vec3.ZERO));
        }
    }
}
