package dev.ashu.rsm.client;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.attack.AttackKind;
import dev.ashu.rsm.power.Bearer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/** Slash, Strike and Dash icons right of the hotbar, shaded from the top while on cooldown. */
public final class AttackHud {
    private static final ResourceLocation SLASH_ICON = RawSuperhumanMod.id("textures/gui/slash.png");
    private static final ResourceLocation STRIKE_ICON = RawSuperhumanMod.id("textures/gui/strike.png");
    private static final ResourceLocation DASH_ICON = RawSuperhumanMod.id("textures/gui/dash.png");
    private static final int ICON = 16;
    private static final int GAP = 4;
    private static final int SHADE = 0xB0000000;

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!RsmConfig.SHOW_HUD.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator() || !Bearer.isBearer(player)) return;

        int now = player.tickCount;
        int x = graphics.guiWidth() / 2 + 95;
        int y = graphics.guiHeight() - 20;
        drawIcon(graphics, SLASH_ICON, x, y, ModKeys.COOLDOWNS.remainingFraction(AttackKind.SLASH, now));
        drawIcon(graphics, STRIKE_ICON, x + ICON + GAP, y, ModKeys.COOLDOWNS.remainingFraction(AttackKind.STRIKE, now));
        drawIcon(graphics, DASH_ICON, x + 2 * (ICON + GAP), y, Dash.remainingFraction(now));
    }

    private static void drawIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y, float remaining) {
        graphics.blit(icon, x, y, 0, 0, ICON, ICON, ICON, ICON);
        if (remaining > 0.0F) {
            graphics.fill(x, y, x + ICON, y + Math.round(remaining * ICON), SHADE);
        }
    }

    private AttackHud() {}
}
