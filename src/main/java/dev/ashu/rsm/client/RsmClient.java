package dev.ashu.rsm.client;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class RsmClient {

    @EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID, value = Dist.CLIENT)
    static final class ModBus {
        @SubscribeEvent
        static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, RawSuperhumanMod.id("flight"), FlightHud::render);
            event.registerAbove(VanillaGuiLayers.HOTBAR, RawSuperhumanMod.id("attacks"), AttackHud::render);
        }
    }

    @EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID, value = Dist.CLIENT)
    static final class GameBus {
        @SubscribeEvent
        static void onMovementInput(MovementInputUpdateEvent event) {
            if (event.getEntity() instanceof LocalPlayer player) {
                FlightController.get().onMovementInput(player, event.getInput());
            }
        }

        @SubscribeEvent
        static void onClientTick(ClientTickEvent.Post event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                FlightController.get().tickPost(player);
                ModKeys.tick(player);
            }
        }

        /** Extra field of view proportional to speed: +fovBoostDegrees at max speed. */
        @SubscribeEvent
        static void onComputeFov(ComputeFovModifierEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (event.getPlayer() != minecraft.player) return;
            FlightController controller = FlightController.get();
            if (!controller.isFast()) return;

            double baseFov = minecraft.options.fov().get();
            double effectScale = minecraft.options.fovEffectScale().get();
            double extra = RsmConfig.FOV_BOOST_DEGREES.get() * controller.speedFraction() * effectScale;
            event.setNewFovModifier((float) (event.getNewFovModifier() * (baseFov + extra) / baseFov));
        }

        @SubscribeEvent
        static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            FlightController.get().reset();
            ModKeys.COOLDOWNS.reset();
            Dash.reset();
        }

        @SubscribeEvent
        static void onClone(ClientPlayerNetworkEvent.Clone event) {
            FlightController.get().reset();
            ModKeys.COOLDOWNS.reset();
            Dash.reset();
        }
    }

    private RsmClient() {}
}
