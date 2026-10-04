package dev.ashu.rsm.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.grab.GrabPose;
import dev.ashu.rsm.network.GrabActionPayload;
import dev.ashu.rsm.network.GrabActionPayload.Action;
import dev.ashu.rsm.power.Bearer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The client side of Grab. Keys: Grab grabs what is under the crosshair; held again for THROW_HOLD_TICKS while
 * holding, it throws; Release lets go. A held player cannot move and struggles by jumping. Every tick, each Captive
 * is put at its hold point next to its Holder as this client sees it, so it follows smoothly however fast it goes.
 */
final class GrabControls {
    /** Holding Grab this long (0.4 s) with a Captive in hand throws it. */
    private static final int THROW_HOLD_TICKS = 8;
    /** How far a struggling Captive's model jerks, blocks. */
    private static final float STRUGGLE_JERK = 0.08F;

    /** A Throw is charging: Grab was pressed anew while already holding. */
    private static boolean charging;
    private static int chargeTicks;
    private static boolean jumpWasDown;
    /** Each Captive's hold point last tick, to draw the movement in between smoothly. */
    private static final Map<Integer, Vec3> LAST_HOLD = new HashMap<>();
    private static boolean shakePushed;

    /** Every client tick, after the game read the keys. */
    static void tick(LocalPlayer player) {
        applyPush(player);
        boolean holding = ClientGrabs.isHolding(player.getId());
        while (ModKeys.GRAB.consumeClick()) {
            // A press that grabs never throws, even if the key stays down until the grab is confirmed.
            if (holding) {
                charging = true;
                chargeTicks = 0;
            } else {
                tryGrab(player);
            }
        }
        if (charging) {
            if (!holding || !ModKeys.GRAB.isDown()) {
                charging = false;
            } else if (++chargeTicks >= THROW_HOLD_TICKS) {
                charging = false;
                PacketDistributor.sendToServer(GrabActionPayload.of(Action.THROW));
            }
        }
        while (ModKeys.RELEASE.consumeClick()) {
            if (holding) PacketDistributor.sendToServer(GrabActionPayload.of(Action.RELEASE));
        }
    }

    private static void tryGrab(LocalPlayer player) {
        if (player.isSpectator() || !Bearer.isBearer(player) || ClientGrabs.isCaptive(player.getId())) return;
        Entity target = pick(player);
        if (target != null) PacketDistributor.sendToServer(new GrabActionPayload(Action.GRAB, target.getId()));
    }

    /** The living thing under the crosshair within grab range, walls in the way. */
    @Nullable
    private static Entity pick(LocalPlayer player) {
        double range = RsmConfig.GRAB_RANGE.get();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) end = block.getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0),
            entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator(), range * range);
        return hit != null ? hit.getEntity() : null;
    }

    /** Before movement: a held player stays put, and every fresh jump is a struggle. */
    static void onMovementInput(LocalPlayer player, Input input) {
        boolean jumpDown = input.jumping;
        boolean jumped = jumpDown && !jumpWasDown;
        jumpWasDown = jumpDown;
        if (!ClientGrabs.isCaptive(player.getId())) return;
        if (jumped) {
            PacketDistributor.sendToServer(GrabActionPayload.of(Action.STRUGGLE));
            ClientGrabs.struggled(player.getId());
            ScreenShake.start(1.5F, 5);
        }
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = input.down = input.left = input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    /** End of every client tick: put each Captive at its hold point, with last tick's point as where it came from. */
    static void placeCaptives(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null) return;
        for (Map.Entry<Integer, Integer> entry : ClientGrabs.holders().entrySet()) {
            Entity captive = level.getEntity(entry.getKey());
            if (captive == null || !(level.getEntity(entry.getValue()) instanceof LivingEntity holder)) continue;
            Vec3 pos = GrabPose.holdPosition(holder, captive, fastDirection(holder, minecraft.player));
            Vec3 last = LAST_HOLD.getOrDefault(captive.getId(), pos);
            captive.setPos(pos);
            captive.xo = captive.xOld = last.x;
            captive.yo = captive.yOld = last.y;
            captive.zo = captive.zOld = last.z;
            captive.setDeltaMovement(Vec3.ZERO);
            LAST_HOLD.put(captive.getId(), pos);
        }
        LAST_HOLD.keySet().removeIf(id -> !ClientGrabs.isCaptive(id));
    }

    /** The Holder's flight line in fast flight, else null: our own from the flight controller, others' from how they moved. */
    @Nullable
    private static Vec3 fastDirection(LivingEntity holder, @Nullable LocalPlayer self) {
        if (holder == self) {
            FlightController controller = FlightController.get();
            return controller.isFast() ? controller.direction() : null;
        }
        if (!holder.isFallFlying()) return null;
        Vec3 moved = holder.position().subtract(holder.xo, holder.yo, holder.zo);
        return moved.lengthSqr() > 1.0E-4 ? moved.normalize() : holder.getLookAngle();
    }

    /** A counterattack threw us off: leave fast flight and fly back with the push. */
    private static void applyPush(LocalPlayer player) {
        float[] push = ClientGrabs.takePush();
        if (push == null) return;
        FlightController.get().stopFastFlight(player);
        player.setDeltaMovement(push[0], push[1], push[2]);
    }

    /** Before a living entity is drawn: a struggling Captive jerks about. Pairs with {@link #afterRender}. */
    static void beforeRender(LivingEntity entity, PoseStack poseStack) {
        float shake = ClientGrabs.struggleShake(entity.getId());
        shakePushed = shake > 0.0F;
        if (!shakePushed) return;
        float t = (Util.getMillis() % 100_000L) * 0.06F;
        poseStack.pushPose();
        poseStack.translate(STRUGGLE_JERK * shake * Mth.sin(t * 7.0F), STRUGGLE_JERK * 0.5F * shake * Mth.sin(t * 9.0F), STRUGGLE_JERK * shake * Mth.sin(t * 5.0F));
    }

    static void afterRender(PoseStack poseStack) {
        if (shakePushed) poseStack.popPose();
        shakePushed = false;
    }

    static void reset() {
        charging = false;
        jumpWasDown = false;
        LAST_HOLD.clear();
        ClientGrabs.reset();
    }

    private GrabControls() {}
}
