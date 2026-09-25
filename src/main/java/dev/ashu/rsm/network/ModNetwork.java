package dev.ashu.rsm.network;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.attack.Attacks;
import dev.ashu.rsm.client.ClientPassives;
import dev.ashu.rsm.power.Bearer;
import dev.ashu.rsm.power.DashEffects;
import dev.ashu.rsm.power.FlightMode;
import dev.ashu.rsm.power.FlightState;
import dev.ashu.rsm.power.PassiveEffectsHandler;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
public final class ModNetwork {

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        // Bump when payloads change: a 0.2 client and a 0.3 server must refuse each other.
        PayloadRegistrar registrar = event.registrar("2");
        registrar.playToServer(FlightStatePayload.TYPE, FlightStatePayload.STREAM_CODEC, ModNetwork::handleFlightState);
        registrar.playToServer(AttackPayload.TYPE, AttackPayload.STREAM_CODEC, ModNetwork::handleAttack);
        registrar.playToServer(DashPayload.TYPE, DashPayload.STREAM_CODEC, ModNetwork::handleDash);
        registrar.playToServer(TogglePassivePayload.TYPE, TogglePassivePayload.STREAM_CODEC, ModNetwork::handleTogglePassive);
        // A lambda, so the client-side target class is only resolved when a packet actually arrives on a client.
        registrar.playToClient(PassiveEffectsPayload.TYPE, PassiveEffectsPayload.STREAM_CODEC,
            (payload, context) -> ClientPassives.set(payload.disabledMask()));
    }

    /** Runs on the server main thread. The client is authoritative for movement; the server only sanity-checks. */
    private static void handleFlightState(FlightStatePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        FlightState state = player.getData(ModAttachments.FLIGHT);

        boolean allowed = payload.mode().isFast() && Bearer.isBearer(player) && player.getAbilities().flying;
        if (!allowed) {
            state.reset();
            return;
        }

        state.mode = payload.mode();
        state.speed = Mth.clamp(payload.speed(), 0.0, RsmConfig.MAX_SPEED.get() * 1.05);
        Vec3 direction = payload.direction();
        state.direction = direction.lengthSqr() > 1.0E-6 ? direction.normalize() : player.getLookAngle();
        state.lastUpdateTick = player.tickCount;
    }

    private static void handleAttack(AttackPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Attacks.perform(player, payload.kind());
        }
    }

    private static void handleDash(DashPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            DashEffects.showToOthers(player);
        }
    }

    private static void handleTogglePassive(TogglePassivePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            PassiveEffectsHandler.toggle(player, payload.effect());
        }
    }

    private ModNetwork() {}
}
