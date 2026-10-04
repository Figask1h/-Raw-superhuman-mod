package dev.ashu.rsm.grab;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.attack.Attacks;
import dev.ashu.rsm.network.GrabStatePayload;
import dev.ashu.rsm.network.PushPayload;
import dev.ashu.rsm.network.ScreenShakePayload;
import dev.ashu.rsm.network.StrugglePayload;
import dev.ashu.rsm.power.Bearer;
import dev.ashu.rsm.power.FlightState;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Grab on the server: who may take whom, keeping each Captive in place, and the ways a Grab ends (Release, Throw,
 * Break Free, or by itself when it can no longer go on). The server decides everything; clients ask with
 * GrabActionPayload and are told the outcome with GrabStatePayload.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
public final class Grabs {
    /** Datapack-editable: creatures that can never be grabbed. */
    public static final TagKey<EntityType<?>> UNGRABBABLE = TagKey.create(Registries.ENTITY_TYPE, RawSuperhumanMod.id("ungrabbable"));
    /** Thrown into a wall or rammed through one; physical (tag rsm:physical), so a Bearer takes 1%. */
    public static final ResourceKey<DamageType> SLAMMED = ResourceKey.create(Registries.DAMAGE_TYPE, RawSuperhumanMod.id("slammed"));

    /** Half a heart to the Captive when the Holder runs into blocks, at most once per SLAM_INTERVAL ticks. */
    private static final float SLAM_DAMAGE = 1.0F;
    private static final int SLAM_INTERVAL = 10;
    /** Jumps count towards Breaking Free for this many ticks. */
    private static final int STRUGGLE_WINDOW = 60;
    /** Jumps closer together than this count once: nobody mashes faster, so faster is a bounce or a cheat. */
    private static final int STRUGGLE_MIN_GAP = 2;
    /** Extra reach over grab.range, for the Holder's latency. */
    private static final double REACH_SLACK = 1.0;
    /** Further apart than this, one of them was teleported away. */
    private static final double MAX_DISTANCE = 24.0;
    /** A counterattack throws the Holder about 5 blocks: air drag keeps a flyer going, the ground needs a hop. */
    private static final double PUSH_FLYING = 0.45;
    private static final double PUSH_GROUND = 1.0;
    private static final double PUSH_GROUND_LIFT = 0.3;
    /** Keeps a held player's own view: with the player's current rotation passed in, the rotation change sent is zero. */
    private static final Set<RelativeMovement> KEEP_ROTATION = EnumSet.of(RelativeMovement.X_ROT, RelativeMovement.Y_ROT);

    private static final Map<ServerPlayer, Grab> BY_HOLDER = new HashMap<>();
    private static final Map<LivingEntity, Grab> BY_CAPTIVE = new HashMap<>();

    private enum End { RELEASE, THROW, BREAK_FREE }

    public static boolean isHolding(Entity entity) {
        return BY_HOLDER.containsKey(entity);
    }

    public static boolean isCaptive(Entity entity) {
        return BY_CAPTIVE.containsKey(entity);
    }

    static DamageSource slammed(ServerPlayer by) {
        return by.level().damageSources().source(SLAMMED, by);
    }

    /** Everything about the target itself; reach and sight are checked by {@link #tryGrab}. */
    public static boolean canGrab(ServerPlayer holder, LivingEntity target) {
        if (target == holder || !target.isAlive() || target.isRemoved() || target.isSpectator()) return false;
        if (target.getType().is(UNGRABBABLE) || target.getMaxHealth() > RsmConfig.GRAB_MAX_HEALTH.get()) return false;
        // Someone else's Captive is Ungrabbable: no stealing.
        if (BY_CAPTIVE.containsKey(target)) return false;
        if (target instanceof Player player) return RsmConfig.GRAB_PLAYERS.get() && !player.isCreative();
        return true;
    }

    /** The Holder pressed Grab with {@code targetId} under the crosshair. */
    public static void tryGrab(ServerPlayer holder, int targetId) {
        if (holder.level().getEntity(targetId) instanceof LivingEntity target) tryGrab(holder, target);
    }

    public static void tryGrab(ServerPlayer holder, LivingEntity target) {
        if (!Bearer.isBearer(holder) || holder.isSpectator() || !holder.isAlive() || holder.isPassenger() || holder.isSleeping()) return;
        if (BY_HOLDER.containsKey(holder) || BY_CAPTIVE.containsKey(holder) || !canGrab(holder, target)) return;
        Vec3 eye = holder.getEyePosition();
        Vec3 closest = Attacks.closestPoint(target.getBoundingBox(), eye);
        double reach = RsmConfig.GRAB_RANGE.get() + REACH_SLACK;
        if (closest.distanceToSqr(eye) > reach * reach) return;
        if (!Attacks.isVisible(holder.serverLevel(), holder, eye, target, closest)) return;
        start(holder, target);
    }

    private static void start(ServerPlayer holder, LivingEntity captive) {
        if (captive instanceof ServerPlayer player) {
            // A Holder who gets grabbed lets go of their own Captive.
            Grab own = BY_HOLDER.get(player);
            if (own != null) end(own, End.RELEASE);
            player.getData(ModAttachments.FLIGHT).reset();
        }
        captive.stopRiding();
        captive.ejectPassengers();
        Grab grab = new Grab(holder, captive);
        BY_HOLDER.put(holder, grab);
        BY_CAPTIVE.put(captive, grab);
        sync(grab, captive.getId());
        holder.level().playSound(null, captive.getX(), captive.getEyeY(), captive.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 0.8F, 1.3F);
    }

    /** Throw along the look; the Holder's own flight Speed is added, so a Captive thrown at full speed is not overtaken. */
    public static void throwCaptive(ServerPlayer holder) {
        Grab grab = BY_HOLDER.get(holder);
        if (grab == null) return;
        FlightState flight = holder.getData(ModAttachments.FLIGHT);
        double speed = RsmConfig.THROW_SPEED.get() + (flight.isFast() ? flight.speed : 0.0);
        end(grab, End.THROW);
        Thrown.start(grab.captive, holder, holder.getLookAngle().scale(speed / 20.0));
        holder.level().playSound(null, holder.getX(), holder.getEyeY(), holder.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    public static void releaseByHolder(ServerPlayer holder) {
        Grab grab = BY_HOLDER.get(holder);
        if (grab != null) end(grab, End.RELEASE);
    }

    /** A held player jumped. Enough jumps in STRUGGLE_WINDOW and they Break Free; mobs never struggle. */
    public static void struggle(ServerPlayer captive) {
        Grab grab = BY_CAPTIVE.get(captive);
        if (grab == null) return;
        long now = captive.level().getGameTime();
        Long last = grab.struggles.peekLast();
        if (last != null && now - last < STRUGGLE_MIN_GAP) return;
        grab.struggles.addLast(now);
        while (now - grab.struggles.getFirst() >= STRUGGLE_WINDOW) grab.struggles.removeFirst();
        PacketDistributor.sendToPlayersTrackingEntity(captive, new StrugglePayload(captive.getId()));
        int needed = Bearer.isBearer(captive) ? RsmConfig.BREAK_FREE_JUMPS_BEARER.get() : RsmConfig.BREAK_FREE_JUMPS.get();
        if (grab.struggles.size() >= needed) end(grab, End.BREAK_FREE);
    }

    /**
     * A Captive Bearer used an attack: instead of hitting, it knocks the Holder about 5 blocks away and Breaks Free.
     * Nobody is hurt; the caller has already put the attack on cooldown. False when not a Captive.
     */
    public static boolean counter(ServerPlayer captive) {
        Grab grab = BY_CAPTIVE.get(captive);
        if (grab == null) return false;
        end(grab, End.BREAK_FREE);
        ServerPlayer holder = grab.holder;
        Vec3 away = holder.position().subtract(captive.position()).multiply(1.0, 0.0, 1.0);
        if (away.lengthSqr() < 1.0E-6) away = captive.getLookAngle().multiply(1.0, 0.0, 1.0);
        away = away.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
        boolean flying = holder.getAbilities().flying;
        Vec3 push = away.scale(flying ? PUSH_FLYING : PUSH_GROUND).add(0.0, flying ? 0.0 : PUSH_GROUND_LIFT, 0.0);
        if (holder.connection != null) {
            // The Holder's client moves the Holder (and may be in fast flight), so it is told to take the push itself.
            PacketDistributor.sendToPlayer(holder, new PushPayload((float) push.x, (float) push.y, (float) push.z));
            PacketDistributor.sendToPlayer(holder, new ScreenShakePayload(2.0F, 8));
        }
        return true;
    }

    /** Breakthrough tells us the Holder tunnelled this tick: for a Captive held ahead that is a slam. */
    public static void markTunnelled(ServerPlayer holder) {
        Grab grab = BY_HOLDER.get(holder);
        if (grab != null) grab.tunnelled = true;
    }

    private static void end(Grab grab, End how) {
        BY_HOLDER.remove(grab.holder);
        BY_CAPTIVE.remove(grab.captive);
        sync(grab, -1);
        if (how == End.BREAK_FREE) anvil(grab.captive);
    }

    /** Break Free: the clang and chips of an anvil, no flash. */
    private static void anvil(LivingEntity captive) {
        if (!(captive.level() instanceof ServerLevel level)) return;
        level.playSound(null, captive.getX(), captive.getEyeY(), captive.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 1.0F);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ANVIL.defaultBlockState()),
            captive.getX(), captive.getY(0.75), captive.getZ(), 30, 0.3, 0.3, 0.3, 0.15);
    }

    /** Everyone who sees the Holder learns who it holds (-1: nobody); a held player always learns it too. */
    private static void sync(Grab grab, int captiveId) {
        GrabStatePayload payload = new GrabStatePayload(grab.holder.getId(), captiveId);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(grab.holder, payload);
        if (grab.captive instanceof ServerPlayer player && player.connection != null) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        for (Grab grab : List.copyOf(BY_HOLDER.values())) {
            if (isOver(grab)) {
                end(grab, End.RELEASE);
                continue;
            }
            FlightState flight = grab.holder.getData(ModAttachments.FLIGHT);
            hold(grab, flight);
            slam(grab, flight);
        }
        Thrown.tickAll();
    }

    /** Release by itself: the Ring came off, someone died, left, turned spectator, mounted, slept or changed dimension. */
    private static boolean isOver(Grab grab) {
        ServerPlayer holder = grab.holder;
        LivingEntity captive = grab.captive;
        if (holder.isRemoved() || !holder.isAlive() || holder.isSpectator() || !Bearer.isBearer(holder)) return true;
        if (holder.isPassenger() || holder.isSleeping()) return true;
        if (captive.isRemoved() || !captive.isAlive() || captive.level() != holder.level()) return true;
        if (captive instanceof Player player && (player.isSpectator() || player.isCreative())) return true;
        return captive.distanceToSqr(holder) > MAX_DISTANCE * MAX_DISTANCE;
    }

    /** After all entities ticked: the Captive goes back to the hold point, whatever its AI or gravity tried. */
    private static void hold(Grab grab, FlightState flight) {
        LivingEntity captive = grab.captive;
        Vec3 pos = GrabPose.holdPosition(grab.holder, captive, flight.isFast() ? flight.direction : null);
        captive.setDeltaMovement(Vec3.ZERO);
        captive.resetFallDistance();
        if (captive instanceof ServerPlayer player && player.connection != null) {
            player.connection.teleport(pos.x, pos.y, pos.z, player.getYRot(), player.getXRot(), KEEP_ROTATION);
        } else {
            captive.setPos(pos);
            if (captive instanceof Mob mob) mob.getNavigation().stop();
        }
    }

    /** The Holder ran into blocks with the Captive in hand: a wall, a ceiling, a crash out of fast flight, a tunnel. */
    private static void slam(Grab grab, FlightState flight) {
        ServerPlayer holder = grab.holder;
        boolean ceiling = holder.verticalCollision && !holder.verticalCollisionBelow;
        boolean crashed = grab.wasFast && holder.verticalCollisionBelow;
        boolean hit = grab.tunnelled || holder.horizontalCollision || ceiling || crashed;
        grab.tunnelled = false;
        grab.wasFast = flight.isFast();
        long now = holder.level().getGameTime();
        if (!hit || now - grab.lastSlam < SLAM_INTERVAL) return;
        grab.lastSlam = now;
        grab.captive.hurt(slammed(holder), SLAM_DAMAGE);
    }

    /** Held in walls and dragged around at speed: no suffocation, cramming or fall damage while held. */
    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!BY_CAPTIVE.containsKey(event.getEntity())) return;
        DamageSource source = event.getSource();
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.FALL)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Grab grab = grabOf(event.getEntity());
        if (grab != null) end(grab, End.RELEASE);
    }

    /** Someone who starts seeing a Holder learns what it holds. */
    @SubscribeEvent
    static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer holder && event.getEntity() instanceof ServerPlayer watcher) {
            Grab grab = BY_HOLDER.get(holder);
            if (grab != null) PacketDistributor.sendToPlayer(watcher, new GrabStatePayload(holder.getId(), grab.captive.getId()));
        }
    }

    @SubscribeEvent
    static void onServerStopped(ServerStoppedEvent event) {
        BY_HOLDER.clear();
        BY_CAPTIVE.clear();
        Thrown.clear();
    }

    @Nullable
    private static Grab grabOf(Entity entity) {
        Grab grab = BY_HOLDER.get(entity);
        return grab != null ? grab : BY_CAPTIVE.get(entity);
    }

    private Grabs() {}
}
