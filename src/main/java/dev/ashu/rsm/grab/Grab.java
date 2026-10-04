package dev.ashu.rsm.grab;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayDeque;

/** One Grab in progress on the server: who holds whom, and what has happened to them since. */
final class Grab {
    final ServerPlayer holder;
    final LivingEntity captive;
    /** Game times of the Captive's recent jumps, oldest first. */
    final ArrayDeque<Long> struggles = new ArrayDeque<>();
    /** Game time the Captive last took damage from the Holder running into blocks. */
    long lastSlam = Long.MIN_VALUE / 2;
    /** Set when the Holder tunnelled through blocks this tick (Breakthrough). */
    boolean tunnelled;
    /** Whether the Holder was in fast flight last tick: landing out of it is a crash. */
    boolean wasFast;

    Grab(ServerPlayer holder, LivingEntity captive) {
        this.holder = holder;
        this.captive = captive;
    }
}
