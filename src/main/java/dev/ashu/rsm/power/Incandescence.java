package dev.ashu.rsm.power;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.network.IncandescencePayload;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Incandescence: the front of a fast Bearer glows like heated metal from incandescence.minSpeed up, brightest at
 * Max Speed. Purely visual. The server turns each Bearer's reported Speed into a heat level and tells the players
 * who can see them; their clients draw it and let it cool. The Bearer's own client uses its own Speed.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
public final class Incandescence {
    /** Heat is sent as a whole number from 0 (cold) to this. */
    public static final int LEVELS = 255;

    /** 0 at incandescence.minSpeed and below, 1 at Max Speed. */
    public static float heat(double speed) {
        double min = RsmConfig.INCANDESCENCE_MIN_SPEED.get();
        double max = RsmConfig.MAX_SPEED.get();
        if (max <= min) return 0.0F;
        return (float) Mth.clamp((speed - min) / (max - min), 0.0, 1.0);
    }

    /** End of every server player tick: when this Bearer's heat changed, tell the players watching. */
    public static void sync(ServerPlayer player, FlightState state) {
        int level = state.isFast() ? Math.round(heat(state.speed) * LEVELS) : 0;
        if (level == state.incandescence) return;
        state.incandescence = level;
        PacketDistributor.sendToPlayersTrackingEntity(player, new IncandescencePayload(player.getId(), level));
    }

    /** A player who starts seeing another learns its heat, 0 included, so a glow remembered from earlier cannot linger. */
    @SubscribeEvent
    static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer watcher) {
            int level = target.getData(ModAttachments.FLIGHT).incandescence;
            PacketDistributor.sendToPlayer(watcher, new IncandescencePayload(target.getId(), level));
        }
    }

    private Incandescence() {}
}
