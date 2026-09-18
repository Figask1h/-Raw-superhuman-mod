package dev.ashu.rsm.client;

import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.power.Bearer;
import dev.ashu.rsm.power.FlightMode;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;

/** Speedometer left of the hotbar while a Bearer is flying, with the CRUISE marker above it. */
public final class FlightHud {
    private static final int SPEED_COLOR = 0xFFE0F7FF;
    private static final int CRUISE_COLOR = 0xFFFFD24D;

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!RsmConfig.SHOW_HUD.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui) return;
        if (!player.getAbilities().flying || !Bearer.isBearer(player)) return;

        FlightController controller = FlightController.get();
        double speed = controller.isFast() ? controller.speed() : player.getDeltaMovement().length() * 20.0;

        Font font = minecraft.font;
        int right = graphics.guiWidth() / 2 - 96;
        int y = graphics.guiHeight() - 16;
        String text = I18n.get("rsm.hud.speed", Math.round(speed));
        graphics.drawString(font, text, right - font.width(text), y, SPEED_COLOR, true);
        if (controller.mode() == FlightMode.CRUISE) {
            String cruise = I18n.get("rsm.hud.cruise");
            graphics.drawString(font, cruise, right - font.width(cruise), y - 10, CRUISE_COLOR, true);
        }
    }

    private FlightHud() {}
}
