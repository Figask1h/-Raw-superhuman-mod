package dev.ashu.rsm.attack;

import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.power.BlockDestruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Clap: a directed blast from the Bearer's eyes, aimed a little below the look. Rays fan out in a cone, each
 * with its own random reach and strength, so the crater is ragged: soft blocks give way along the whole range,
 * stone only a few layers deep. A ray stops dead at a block the Bearer may not destroy (an Impervious Block,
 * a claim), so everything behind it is shielded. Drops fall like an explosion's. Then everything living in the
 * cone and in view takes the damage and is thrown away from the Bearer.
 */
final class Clap {
    private static final int RAYS = 600;
    private static final double STEP = 0.3;
    /** Loot is rolled as for an explosion of this radius: each drop survives with chance 1 / radius. */
    private static final float DROP_RADIUS = 4.0F;
    private static final int EFFECT_EVERY = 8;
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);

    static void perform(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        float pitch = Mth.clamp(player.getXRot() + RsmConfig.CLAP_TILT_DEGREES.get().floatValue(), -90.0F, 90.0F);
        Vec3 aim = Vec3.directionFromRotation(pitch, player.getYRot());
        double range = AttackKind.CLAP.range();
        double halfCone = Math.toRadians(RsmConfig.CLAP_CONE_DEGREES.get() / 2.0);

        if (RsmConfig.DESTROY_BLOCKS.get()) blast(player, level, eye, aim, range, halfCone);
        hitLiving(player, level, eye, aim, range, halfCone);

        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.4F);
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 0.6F);
        for (int i = 1; i <= 3; i++) {
            Vec3 at = eye.add(aim.scale(range * i / 4.0));
            level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @SuppressWarnings("deprecation") // block-level blast resistance, see BlockDestruction
    private static void blast(ServerPlayer player, ServerLevel level, Vec3 eye, Vec3 aim, double range, double halfCone) {
        RandomSource random = player.getRandom();
        Vec3 side = aim.cross(Math.abs(aim.y) < 0.99 ? UP : new Vec3(1.0, 0.0, 0.0)).normalize();
        Vec3 up = side.cross(aim);
        Map<BlockPos, Boolean> mayDestroy = new HashMap<>();
        Set<BlockPos> doomed = new LinkedHashSet<>();

        for (int i = 0; i < RAYS; i++) {
            // Uniform over the cone's spherical cap.
            double cos = 1.0 - random.nextDouble() * (1.0 - Math.cos(halfCone));
            double sin = Math.sqrt(1.0 - cos * cos);
            double phi = random.nextDouble() * Math.PI * 2.0;
            Vec3 ray = aim.scale(cos).add(side.scale(sin * Math.cos(phi))).add(up.scale(sin * Math.sin(phi)));
            double reach = range * (0.8 + random.nextDouble() * 0.4);
            double power = RsmConfig.CLAP_POWER.get() * (0.7 + random.nextDouble() * 0.6);

            for (double d = 0.0; d <= reach; d += STEP) {
                BlockPos pos = BlockPos.containing(eye.add(ray.scale(d)));
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.getBlock() instanceof LiquidBlock) continue;
                if (!mayDestroy.computeIfAbsent(pos, p -> BlockDestruction.mayDestroy(player, p, state))) break;
                // Same cost per step as a vanilla explosion ray.
                power -= (state.getBlock().getExplosionResistance() + 0.3) * STEP;
                if (power < 0.0) break;
                doomed.add(pos);
            }
        }

        int destroyed = 0;
        for (BlockPos pos : doomed) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            if (destroyed++ % EFFECT_EVERY == 0) level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            LootParams.Builder loot = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withParameter(LootContextParams.EXPLOSION_RADIUS, DROP_RADIUS)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player);
            state.spawnAfterBreak(level, pos, ItemStack.EMPTY, true);
            state.getDrops(loot).forEach(stack -> Block.popResource(level, pos, stack));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void hitLiving(ServerPlayer player, ServerLevel level, Vec3 eye, Vec3 aim, double range, double halfCone) {
        DamageSource source = player.damageSources().explosion(player, player);
        float damage = (float) AttackKind.CLAP.damage();
        double knockback = RsmConfig.CLAP_KNOCKBACK.get();
        AABB area = new AABB(eye, eye).inflate(range + 1.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, t -> Attacks.canTarget(player, t))) {
            Vec3 closest = Attacks.closestPoint(target.getBoundingBox(), eye);
            Vec3 toTarget = closest.subtract(eye);
            if (toTarget.lengthSqr() > range * range) continue;
            if (toTarget.lengthSqr() > 1.0E-4 && Attacks.angleBetween(aim, toTarget) > halfCone) continue;
            if (!Attacks.isVisible(level, player, eye, target, closest)) continue;
            target.hurt(source, damage);
            Vec3 away = target.position().subtract(player.position());
            target.knockback(knockback, -away.x, -away.z);
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.3, 0.0));
            target.hurtMarked = true;
        }
    }

    private Clap() {}
}
