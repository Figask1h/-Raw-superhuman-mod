package dev.ashu.rsm.client;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.item.ToggleableItem;
import dev.ashu.rsm.power.Barrier;
import dev.ashu.rsm.power.Incandescence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class RsmClient {
    /** Share of fovBoostDegrees that comes as a step while Speed crosses the Barrier; the rest follows the curve. */
    private static final double BARRIER_FOV_SHARE = 0.15;
    /** The Barrier step is spread over this many blocks/second either side of the Barrier. */
    private static final double BARRIER_FOV_HALF_WIDTH = 5.0;

    @EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID, value = Dist.CLIENT)
    static final class ModBus {
        @SubscribeEvent
        static void registerGuiLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, RawSuperhumanMod.id("flight"), FlightHud::render);
            event.registerAbove(VanillaGuiLayers.HOTBAR, RawSuperhumanMod.id("abilities"), AbilityHud::render);
        }
    }

    @EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID, value = Dist.CLIENT)
    static final class GameBus {
        @SubscribeEvent
        static void onMovementInput(MovementInputUpdateEvent event) {
            if (event.getEntity() instanceof LocalPlayer player) {
                // First: a held player's input is swallowed before flight sees it.
                GrabControls.onMovementInput(player, event.getInput());
                FlightController.get().onMovementInput(player, event.getInput());
            }
        }

        @SubscribeEvent
        static void onClientTick(ClientTickEvent.Post event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                FlightController controller = FlightController.get();
                controller.tickPost(player);
                ModKeys.tick(player);
                GrabControls.tick(player);
                // Our own glow follows our own Speed; the server only tells the others.
                IncandescenceGlow.setTarget(player.getId(), controller.isFast() ? Incandescence.heat(controller.speed()) : 0.0F);
                IncandescenceGlow.tick(id -> player.level().getEntity(id) != null);
            }
            ScreenShake.tick();
            GrabControls.placeCaptives(Minecraft.getInstance());
        }

        @SubscribeEvent
        static void beforeLivingRender(RenderLivingEvent.Pre<?, ?> event) {
            GrabControls.beforeRender(event.getEntity(), event.getPoseStack());
        }

        @SubscribeEvent
        static void afterLivingRender(RenderLivingEvent.Post<?, ?> event) {
            GrabControls.afterRender(event.getPoseStack());
        }

        /** Extra field of view in fast flight: none at hover speed, +fovBoostDegrees at max speed. */
        @SubscribeEvent
        static void onComputeFov(ComputeFovModifierEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (event.getPlayer() != minecraft.player) return;
            FlightController controller = FlightController.get();
            if (!controller.isFast()) return;

            double baseFov = minecraft.options.fov().get();
            double effectScale = minecraft.options.fovEffectScale().get();
            double extra = RsmConfig.FOV_BOOST_DEGREES.get() * fovBoostFraction(controller) * effectScale;
            event.setNewFovModifier((float) (event.getNewFovModifier() * (baseFov + extra) / baseFov));
        }

        @SubscribeEvent
        static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (!RsmConfig.SCREEN_SHAKE.get()) return;
            Minecraft minecraft = Minecraft.getInstance();
            float partialTick = (float) event.getPartialTick();
            float amplitude = ScreenShake.amplitude(partialTick) * minecraft.options.screenEffectScale().get().floatValue();
            if (amplitude <= 0.0F || minecraft.player == null) return;
            // Three unrelated wobbles around 7-9 Hz, so the shake never settles into a visible pattern.
            float t = minecraft.player.tickCount + partialTick;
            event.setYaw(event.getYaw() + amplitude * Mth.sin(t * 2.7F));
            event.setPitch(event.getPitch() + amplitude * Mth.sin(t * 3.3F + 1.0F));
            event.setRoll(event.getRoll() + amplitude * 0.5F * Mth.sin(t * 2.1F + 2.0F));
        }

        /** While the local player claps, both bare arms replace the usual first-person hands. */
        @SubscribeEvent
        static void onRenderHand(RenderHandEvent event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            float progress = ClapAnimation.progress(player.getId());
            if (progress < 0.0F) return;
            event.setCanceled(true);
            if (event.getHand() == InteractionHand.MAIN_HAND && !player.isInvisible()) {
                FirstPersonClap.render(player, progress, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
            }
        }

        @SubscribeEvent
        static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            FlightController.get().reset();
            ModKeys.COOLDOWNS.reset();
            Dash.reset();
            ClientPassives.reset();
            ToggleableItem.resetSynced();
            ScreenShake.reset();
            ClapAnimation.reset();
            IncandescenceGlow.reset();
            GrabControls.reset();
        }

        @SubscribeEvent
        static void onClone(ClientPlayerNetworkEvent.Clone event) {
            FlightController.get().reset();
            ModKeys.COOLDOWNS.reset();
            Dash.reset();
            ScreenShake.reset();
        }
    }

    /**
     * Extra FOV as a share of fovBoostDegrees: grows quickly at first (square root of the speed fraction), and a
     * part of it arrives as a step while Speed crosses the Barrier, so breaking it is felt.
     */
    static double fovBoostFraction(FlightController controller) {
        double curve = Math.sqrt(controller.speedFraction());
        double t = (controller.speed() - Barrier.speed() + BARRIER_FOV_HALF_WIDTH) / (2.0 * BARRIER_FOV_HALF_WIDTH);
        double step = Mth.smoothstep(Mth.clamp(t, 0.0, 1.0));
        return (1.0 - BARRIER_FOV_SHARE) * curve + BARRIER_FOV_SHARE * step;
    }

    private RsmClient() {}
}
