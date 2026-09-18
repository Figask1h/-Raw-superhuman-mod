package dev.ashu.rsm.power;

import dev.ashu.rsm.client.ClientFlightHooks;
import dev.ashu.rsm.mixin.EntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Bridges mixins and common code to the side-specific flight logic. */
public final class FlightHooks {
    private static final int FALL_FLYING_FLAG = 7;

    /** Called from the Player.travel mixin. Only the locally controlled client player is moved by the mod. */
    public static boolean overrideTravel(Player player) {
        if (!player.level().isClientSide) return false;
        return ClientFlightHooks.overrideTravel(player);
    }

    public static void setFallFlying(Entity entity, boolean fallFlying) {
        ((EntityAccessor) entity).rsm$setSharedFlag(FALL_FLYING_FLAG, fallFlying);
    }

    private FlightHooks() {}
}
