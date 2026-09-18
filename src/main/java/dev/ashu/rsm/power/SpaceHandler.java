package dev.ashu.rsm.power;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.compat.adastra.AdAstraBridge;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Ascent: a flying Bearer crossing the Ad Astra atmosphere-leave height gets the planet selection
 * screen, like a rocket. Re-arms only after descending {@link #REARM_DEPTH} blocks below the line,
 * so closing the screen without choosing does not reopen it every tick. No-op without Ad Astra.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
public final class SpaceHandler {
    private static final boolean AD_ASTRA_LOADED = ModList.get().isLoaded("ad_astra");
    private static final int REARM_DEPTH = 50;

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!AD_ASTRA_LOADED || !RsmConfig.SPACE_ENABLED.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FlightState state = player.getData(ModAttachments.FLIGHT);

        int leaveHeight = AdAstraBridge.atmosphereLeave();
        if (player.getY() < leaveHeight - REARM_DEPTH) {
            state.ascentArmed = true;
            return;
        }
        if (!state.ascentArmed || player.getY() < leaveHeight) return;
        if (!player.getAbilities().flying || player.isPassenger() || !Bearer.isBearer(player)) return;
        if (!AdAstraBridge.isPlanet(player.serverLevel()) || AdAstraBridge.isPlanetScreenOpen(player)) return;

        state.ascentArmed = false;
        AdAstraBridge.openPlanetScreen(player);
    }

    private SpaceHandler() {}
}
