package dev.ashu.rsm.grab;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Where a Captive is held. Standing or hovering: in front of the Holder by its yaw, neck at the Holder's hand,
 * lifted off the ground if it is too tall for that. In fast flight: ahead along the flight line, like a ram.
 * Server and clients compute it the same way, so the Captive follows the Holder smoothly on every screen.
 */
public final class GrabPose {
    /** Space between the Holder's and the Captive's boxes, so neither pushes the other. */
    private static final double GAP = 0.2;
    /** In fast flight the body lies along the flight line, about this long from the feet to the top of the head. */
    private static final double FLYING_BODY_LENGTH = 1.8;
    /** The hand of an arm held out in front, as a share of the Holder's eye height. */
    private static final double GRIP_HEIGHT = 0.8;
    /** A Captive too tall to hang from the hand is lifted to this height above the Holder's feet. */
    private static final double LIFT = 0.15;

    /** Where the Captive's feet go. {@code fastDirection}: the unit flight direction in fast flight, else null. */
    public static Vec3 holdPosition(LivingEntity holder, Entity captive, @Nullable Vec3 fastDirection) {
        if (fastDirection != null) {
            Vec3 center = holder.position().add(fastDirection.scale(FLYING_BODY_LENGTH + GAP + radius(captive)));
            return center.subtract(0.0, captive.getBbHeight() / 2.0, 0.0);
        }
        float yaw = holder.getYRot() * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
        double reach = holder.getBbWidth() / 2.0 + GAP + captive.getBbWidth() / 2.0;
        double gripY = holder.getY() + holder.getEyeHeight() * GRIP_HEIGHT;
        double feetY = Math.max(holder.getY() + LIFT, gripY - captive.getEyeHeight());
        return holder.position().add(forward.scale(reach)).with(Direction.Axis.Y, feetY);
    }

    /** How far ahead of the Holder's feet a ram reaches in fast flight: what Breakthrough must clear. */
    public static double ramReach(Entity captive) {
        return FLYING_BODY_LENGTH + GAP + 2.0 * radius(captive);
    }

    private static double radius(Entity entity) {
        return Math.max(entity.getBbWidth(), entity.getBbHeight()) / 2.0;
    }

    private GrabPose() {}
}
