package dev.ashu.rsm.power;

import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Breakthrough: a Bearer flying head-on into blocks fast enough destroys a tunnel through them instead of
 * stopping. The client works out the tunnel (it moves the player, so it must clear the way the same tick),
 * removes the blocks locally and sends the list; the server destroys what it agrees to and puts the rest back
 * on the client. No drops, no speed lost.
 */
public final class Breakthrough {
    /** Blocks whose centre is this close to the flight line are the tunnel: about 3x3 across. */
    private static final double TUNNEL_RADIUS = 1.5;
    /** Beyond the tunnel, up to this far, some blocks are chipped away so the edges are ragged. */
    private static final double CHIP_RADIUS = 2.5;
    private static final int CHIP_CHANCE_PERCENT = 30;
    /** Blocks this close to the flight line are in the body's own path: an Impervious one there stops the flight. */
    private static final double BODY_RADIUS = 0.8;
    /** Break sound and particles for every Nth destroyed block, so a tunnel is audible without flooding clients. */
    private static final int EFFECT_EVERY = 12;
    public static final int MAX_BLOCKS_PER_PACKET = 1024;

    /**
     * The blocks to destroy so the Bearer can fly {@code length} blocks from {@code center} along the unit
     * vector {@code direction}, nearest first. Empty when the body's path is blocked by an Impervious Block
     * right away; otherwise the tunnel ends before the first Impervious Block in the body's path.
     * Deterministic: the ragged edge depends only on block positions.
     */
    public static List<BlockPos> tunnel(Level level, Vec3 center, Vec3 direction, double length) {
        record Candidate(BlockPos pos, double along, boolean inBodyPath, BlockState state) {}
        List<Candidate> candidates = new ArrayList<>();
        AABB area = new AABB(center, center.add(direction.scale(length))).inflate(CHIP_RADIUS + 0.5);
        for (BlockPos pos : BlockPos.betweenClosed(
            Mth.floor(area.minX), Mth.floor(area.minY), Mth.floor(area.minZ),
            Mth.floor(area.maxX), Mth.floor(area.maxY), Mth.floor(area.maxZ))) {
            Vec3 offset = Vec3.atCenterOf(pos).subtract(center);
            double along = offset.dot(direction);
            if (along < -0.5 || along > length + 0.5) continue;
            double across = offset.subtract(direction.scale(along)).length();
            if (across > TUNNEL_RADIUS && (across > CHIP_RADIUS || !isChipped(pos))) continue;
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getBlock() instanceof LiquidBlock) continue;
            candidates.add(new Candidate(pos.immutable(), along, across <= BODY_RADIUS, state));
        }
        candidates.sort(Comparator.comparingDouble(Candidate::along));

        List<BlockPos> tunnel = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (!BlockDestruction.isImpervious(level, candidate.pos(), candidate.state())) {
                tunnel.add(candidate.pos());
            } else if (candidate.inBodyPath()) {
                boolean bodyPathClearedBefore = tunnel.stream().anyMatch(pos ->
                    Vec3.atCenterOf(pos).subtract(center).cross(direction).length() <= BODY_RADIUS);
                return bodyPathClearedBefore ? tunnel : List.of();
            }
            if (tunnel.size() >= MAX_BLOCKS_PER_PACKET) break;
        }
        return tunnel;
    }

    private static boolean isChipped(BlockPos pos) {
        // A cheap fixed hash of the position (Fibonacci hashing), so client and server chip the same blocks.
        return (int) ((pos.asLong() * 0x9E3779B97F4A7C15L) >>> 40) % 100 < CHIP_CHANCE_PERCENT;
    }

    public static boolean isFastEnough(double speed) {
        return RsmConfig.DESTROY_BLOCKS.get() && speed >= RsmConfig.BREAKTHROUGH_MIN_SPEED.get();
    }

    /** Server side: destroy the blocks the client cleared, where allowed; send back the ones refused. */
    public static void breakOnServer(ServerPlayer player, List<BlockPos> positions) {
        FlightState flight = player.getData(ModAttachments.FLIGHT);
        // 5% slack: the speed report and the tunnel of the same tick can straddle the threshold.
        boolean allowed = Bearer.isBearer(player) && flight.isFast() && isFastEnough(flight.speed * 1.05);
        // One tick of flight at top speed ahead and behind, plus the tunnel's width.
        double reach = RsmConfig.MAX_SPEED.get() / 20.0 * 2.0 + CHIP_RADIUS + 4.0;
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        int broken = 0;
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            boolean ok = allowed && pos.distToCenterSqr(origin) <= reach * reach
                && !(state.getBlock() instanceof LiquidBlock) && BlockDestruction.mayDestroy(player, pos, state);
            if (!ok) {
                if (player.connection != null) player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
                continue;
            }
            if (broken++ % EFFECT_EVERY == 0) level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private Breakthrough() {}
}
