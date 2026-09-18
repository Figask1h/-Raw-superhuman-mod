package dev.ashu.rsm.power;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server side of flight: keeps the elytra pose and the fall-flying flag in sync with the state
 * reported by the client, and drops fast flight when its preconditions disappear.
 * Creative-style flight itself comes from the creative_flight attribute the Ring provides.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
public final class FlightServerHandler {
    /** Fast flight expires if the client stops reporting for this many ticks. */
    private static final int STALE_TICKS = 40;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FlightState state = player.getData(ModAttachments.FLIGHT);

        if (state.isFast()) {
            boolean stale = player.tickCount - state.lastUpdateTick > STALE_TICKS;
            if (stale || player.isPassenger() || !player.getAbilities().flying || !Bearer.isBearer(player)) {
                state.reset();
            }
        }

        if (state.isFast()) {
            // Vanilla clears flag 7 every tick without an elytra; re-set it after the tick so the
            // synced value clients receive is "true" and remote players render the elytra pose.
            FlightHooks.setFallFlying(player, true);
            player.setForcedPose(Pose.FALL_FLYING);
        } else if (player.getForcedPose() == Pose.FALL_FLYING) {
            player.setForcedPose(null);
        }
    }

    private FlightServerHandler() {}
}
