package dev.ashu.rsm.attack;

import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.grab.Grabs;
import dev.ashu.rsm.network.ClapPayload;
import dev.ashu.rsm.power.Bearer;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-side execution of Slash, Strike and Clap. The client only asks; the server checks Bearer status and cooldowns. */
public final class Attacks {

    public static void perform(ServerPlayer player, AttackKind kind) {
        if (!Bearer.isBearer(player) || player.isSpectator() || !player.isAlive()) return;
        // Both hands are needed for a Clap, and one of them is holding a Captive.
        if (kind == AttackKind.CLAP && Grabs.isHolding(player)) return;
        AttackCooldowns cooldowns = player.getData(ModAttachments.ATTACKS);
        if (!cooldowns.isReady(kind, player.tickCount)) return;
        cooldowns.trigger(kind, player.tickCount, kind.cooldownTicks());
        // A Captive Bearer's attack throws the Holder off instead of hitting anything.
        if (Grabs.counter(player)) return;

        // The client already animated its own player; only other players need to see it.
        if (kind == AttackKind.CLAP) {
            PacketDistributor.sendToPlayersTrackingEntity(player, new ClapPayload(player.getId()));
        } else {
            player.swing(InteractionHand.MAIN_HAND, false);
        }
        switch (kind) {
            case SLASH -> slash(player);
            case STRIKE -> strike(player);
            case CLAP -> Clap.perform(player);
        }
    }

    private static void slash(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double range = AttackKind.SLASH.range();
        double halfArc = Math.toRadians(RsmConfig.SLASH_ARC_DEGREES.get() / 2.0);
        float damage = (float) AttackKind.SLASH.damage();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        DamageSource source = player.damageSources().playerAttack(player);

        boolean hitSomething = false;
        AABB area = player.getBoundingBox().inflate(range + 1.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, t -> canTarget(player, t))) {
            Vec3 closest = closestPoint(target.getBoundingBox(), eye);
            if (closest.distanceToSqr(eye) > range * range) continue;
            Vec3 toTarget = closest.subtract(eye);
            if (toTarget.lengthSqr() > 1.0E-4 && angleBetween(look, toTarget) > halfArc) continue;
            if (!isVisible(level, player, eye, target, closest)) continue;
            if (target.hurt(source, damage)) hitSomething = true;
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (hitSomething) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 0.9F);
        }
        double dx = -Mth.sin(player.getYRot() * Mth.DEG_TO_RAD);
        double dz = Mth.cos(player.getYRot() * Mth.DEG_TO_RAD);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + dx, player.getY(0.5), player.getZ() + dz, 0, dx, 0.0, dz, 0.0);
    }

    private static void strike(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double range = AttackKind.STRIKE.range();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(range));

        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eye, end, searchBox,
            entity -> entity instanceof LivingEntity living && canTarget(player, living), range * range);

        // The trail runs along the whole reach, up to the first wall.
        AttackEffects.sonicTrail(level, eye, look, eye.distanceTo(end));
        AttackEffects.shakeAttacker(player, AttackKind.STRIKE);
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            AttackEffects.flash(level, entityHit.getLocation());
            if (target.hurt(player.damageSources().playerAttack(player), (float) AttackKind.STRIKE.damage())) {
                AttackEffects.shakeHit(target, AttackKind.STRIKE);
            }
            // A Captive stays in the Holder's hand: it takes the damage but not the knockback.
            if (!Grabs.isCaptive(target)) {
                target.knockback(RsmConfig.STRIKE_KNOCKBACK.get(), -look.x, -look.z);
                target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.3, 0.0));
                target.hurtMarked = true;
            }
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(), 16, 0.3, 0.3, 0.3, 0.3);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.8F);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 0.7F);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
    }

    static boolean canTarget(ServerPlayer player, Entity entity) {
        if (entity == player || !entity.isAlive() || entity.isSpectator() || !entity.isAttackable()) return false;
        if (entity instanceof ArmorStand stand && stand.isMarker()) return false;
        return !player.isAlliedTo(entity);
    }

    /** Walls stop the Slash: some part of the target (nearest point, centre or eyes) must be in plain view. */
    public static boolean isVisible(ServerLevel level, ServerPlayer player, Vec3 eye, LivingEntity target, Vec3 closest) {
        for (Vec3 point : new Vec3[] {closest, target.getBoundingBox().getCenter(), target.getEyePosition()}) {
            ClipContext ray = new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            if (level.clip(ray).getType() == HitResult.Type.MISS) return true;
        }
        return false;
    }

    public static Vec3 closestPoint(AABB box, Vec3 point) {
        return new Vec3(
            Mth.clamp(point.x, box.minX, box.maxX),
            Mth.clamp(point.y, box.minY, box.maxY),
            Mth.clamp(point.z, box.minZ, box.maxZ));
    }

    static double angleBetween(Vec3 a, Vec3 b) {
        double denominator = a.length() * b.length();
        if (denominator < 1.0E-8) return 0.0;
        return Math.acos(Mth.clamp(a.dot(b) / denominator, -1.0, 1.0));
    }

    private Attacks() {}
}
