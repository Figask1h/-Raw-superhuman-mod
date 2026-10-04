package dev.ashu.rsm.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.attack.AttackKind;
import dev.ashu.rsm.power.Bearer;
import dev.ashu.rsm.power.PassiveEffect;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * Icon row right of the hotbar: Slash, Strike, Clap and Dash, shaded from the top while on cooldown; Clap faded
 * while a Captive is held (it needs both hands). Above it, flush with its right edge, a row of smaller Passive
 * Effect icons, faded while switched off.
 */
public final class AbilityHud {
    private static final ResourceLocation SLASH_ICON = RawSuperhumanMod.id("textures/gui/slash.png");
    private static final ResourceLocation STRIKE_ICON = RawSuperhumanMod.id("textures/gui/strike.png");
    private static final ResourceLocation CLAP_ICON = RawSuperhumanMod.id("textures/gui/clap.png");
    private static final ResourceLocation DASH_ICON = RawSuperhumanMod.id("textures/gui/dash.png");
    private static final int ICON = 16;
    private static final int GAP = 4;
    private static final int ABILITY_COUNT = 4;
    private static final int PASSIVE_ICON = 12;
    private static final int PASSIVE_GAP = 2;
    /** Space between the Passive Effect row and the ability row below it. */
    private static final int ROW_GAP = 2;
    private static final int SHADE = 0xB0000000;
    private static final float SWITCHED_OFF_ALPHA = 0.3F;

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!RsmConfig.SHOW_HUD.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator() || !Bearer.isBearer(player)) return;

        int now = player.tickCount;
        int left = graphics.guiWidth() / 2 + 95;
        int y = graphics.guiHeight() - 20;
        int x = left;
        drawIcon(graphics, SLASH_ICON, x, y, ModKeys.COOLDOWNS.remainingFraction(AttackKind.SLASH, now), 1.0F);
        x += ICON + GAP;
        drawIcon(graphics, STRIKE_ICON, x, y, ModKeys.COOLDOWNS.remainingFraction(AttackKind.STRIKE, now), 1.0F);
        x += ICON + GAP;
        boolean holding = ClientGrabs.isHolding(player.getId());
        drawIcon(graphics, CLAP_ICON, x, y, ModKeys.COOLDOWNS.remainingFraction(AttackKind.CLAP, now), holding ? SWITCHED_OFF_ALPHA : 1.0F);
        x += ICON + GAP;
        drawIcon(graphics, DASH_ICON, x, y, Dash.remainingFraction(now), 1.0F);

        PassiveEffect[] passives = PassiveEffect.values();
        int right = left + ABILITY_COUNT * ICON + (ABILITY_COUNT - 1) * GAP;
        x = right - passives.length * PASSIVE_ICON - (passives.length - 1) * PASSIVE_GAP;
        y -= PASSIVE_ICON + ROW_GAP;
        for (PassiveEffect effect : passives) {
            drawPassive(minecraft, graphics, effect, x, y, ClientPassives.isEnabled(effect));
            x += PASSIVE_ICON + PASSIVE_GAP;
        }
    }

    private static void drawIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y, float remaining, float alpha) {
        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(icon, x, y, 0, 0, ICON, ICON, ICON, ICON);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (remaining > 0.0F) {
            graphics.fill(x, y, x + ICON, y + Math.round(remaining * ICON), SHADE);
        }
    }

    /** The vanilla effect icon, the same one the effect list shows. */
    private static void drawPassive(Minecraft minecraft, GuiGraphics graphics, PassiveEffect effect, int x, int y, boolean enabled) {
        TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(effect.effect());
        RenderSystem.enableBlend();
        graphics.setColor(1.0F, 1.0F, 1.0F, enabled ? 1.0F : SWITCHED_OFF_ALPHA);
        graphics.blit(x, y, 0, PASSIVE_ICON, PASSIVE_ICON, sprite);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private AbilityHud() {}
}
