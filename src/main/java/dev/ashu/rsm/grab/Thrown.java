package dev.ashu.rsm.grab;

import dev.ashu.rsm.RsmConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A Captive in flight after a Throw. It keeps the throw's velocity for {@link #STRAIGHT_TICKS} (no gravity, no
 * drag, it hits nothing living on the way), then falls as usual. Its first block hit, during or after the
 * straight part, costs throwImpactDamage; fall damage comes on top the vanilla way.
 */
final class Thrown {
    private static final int STRAIGHT_TICKS = 40;
    /** Long enough to land from any sensible height; a Captive still in the air after that is left alone. */
    private static final int GIVE_UP_TICKS = 200;
    /** Players move themselves: their first ticks still report where they stood before the throw reached them. */
    private static final int PLAYER_GRACE_TICKS = 3;

    private static final List<Thrown> ACTIVE = new ArrayList<>();

    private final LivingEntity entity;
    private final ServerPlayer thrower;
    private final Vec3 velocity;
    private int age;

    private Thrown(LivingEntity entity, ServerPlayer thrower, Vec3 velocity) {
        this.entity = entity;
        this.thrower = thrower;
        this.velocity = velocity;
    }

    /** {@code velocity} in blocks per tick. */
    static void start(LivingEntity entity, ServerPlayer thrower, Vec3 velocity) {
        ACTIVE.removeIf(thrown -> thrown.entity == entity);
        ACTIVE.add(new Thrown(entity, thrower, velocity));
        entity.setDeltaMovement(velocity);
        entity.hurtMarked = true;
    }

    /** After every server tick, once all entities have moved. */
    static void tickAll() {
        ACTIVE.removeIf(Thrown::tick);
    }

    static void clear() {
        ACTIVE.clear();
    }

    /** True when this throw is over. */
    private boolean tick() {
        if (entity.isRemoved() || !entity.isAlive() || Grabs.isCaptive(entity)) return true;
        age++;
        boolean hit = entity.horizontalCollision || entity.verticalCollision;
        if (hit && age > (entity instanceof Player ? PLAYER_GRACE_TICKS : 0)) {
            entity.hurt(Grabs.slammed(thrower), RsmConfig.THROW_IMPACT_DAMAGE.get().floatValue());
            return true;
        }
        if (age <= STRAIGHT_TICKS) {
            // Undo the tick's gravity and drag; hurtMarked sends it on, so player clients fly straight too.
            entity.setDeltaMovement(velocity);
            entity.hurtMarked = true;
        }
        return age >= GIVE_UP_TICKS;
    }
}
