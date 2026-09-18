package dev.ashu.rsm.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

/** Client-only entry points reached from common code; must only be called when level.isClientSide. */
public final class ClientFlightHooks {

    public static boolean overrideTravel(Player player) {
        return player instanceof LocalPlayer local && FlightController.get().travel(local);
    }

    private ClientFlightHooks() {}
}
