package dev.ashu.rsm.power;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.network.PassiveEffectsPayload;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server side of the Passive Effect switches: flips them on request and keeps the client's HUD copy current. */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
public final class PassiveEffectsHandler {

    public static void toggle(ServerPlayer player, PassiveEffect effect) {
        PassiveEffects switches = player.getData(ModAttachments.PASSIVES);
        switches.toggle(effect);
        if (!switches.isEnabled(effect)) effect.remove(player);
        sync(player);
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    private static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new PassiveEffectsPayload(player.getData(ModAttachments.PASSIVES).disabledMask()));
    }

    private PassiveEffectsHandler() {}
}
