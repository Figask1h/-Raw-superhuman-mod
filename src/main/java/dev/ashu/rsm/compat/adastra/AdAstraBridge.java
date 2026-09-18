package dev.ashu.rsm.compat.adastra;

import dev.ashu.rsm.power.Bearer;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.common.config.AdAstraConfig;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.menus.base.PlanetsMenuProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * The only class that touches Ad Astra types. Load it only after checking that Ad Astra is present
 * (see {@link dev.ashu.rsm.power.SpaceHandler}); the JVM resolves these imports lazily.
 */
public final class AdAstraBridge {

    /** Height above which a rocket would open the planet screen; the Bearer uses the same line. */
    public static int atmosphereLeave() {
        return AdAstraConfig.atmosphereLeave;
    }

    /** True for dimensions Ad Astra knows as planets or orbits: only those have a sky to leave. */
    public static boolean isPlanet(ServerLevel level) {
        return PlanetApi.API.getPlanet(level) != null;
    }

    public static boolean isPlanetScreenOpen(ServerPlayer player) {
        return player.containerMenu instanceof PlanetsMenu;
    }

    /** Opens the planet selection exactly like a rocket does. Without a rocket the menu grants tier 100: every planet. */
    public static void openPlanetScreen(ServerPlayer player) {
        new PlanetsMenuProvider().openMenu(player);
    }

    /**
     * Mixin hook for ModUtils.canTeleportToPlanet, which otherwise demands a rocket for survival players.
     * A Bearer above the atmosphere with the planet screen open may travel anywhere not disabled in the Ad Astra config.
     */
    public static boolean bearerMayTravel(Player player, Planet planet) {
        if (!Bearer.isBearer(player)) return false;
        if (!(player.containerMenu instanceof PlanetsMenu)) return false;
        if (player.getY() < AdAstraConfig.atmosphereLeave) return false;
        String target = planet.dimension().location().toString();
        for (String disabled : AdAstraConfig.disabledPlanets.split(",")) {
            if (disabled.trim().equals(target)) return false;
        }
        return true;
    }

    private AdAstraBridge() {}
}
