package dev.ashu.rsm.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.attack.AttackCooldowns;
import dev.ashu.rsm.attack.AttackKind;
import dev.ashu.rsm.network.AttackPayload;
import dev.ashu.rsm.network.TogglePassivePayload;
import dev.ashu.rsm.power.Bearer;
import dev.ashu.rsm.power.PassiveEffect;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings (all rebindable in Controls): attacks on mouse 4 / mouse 5 / G, Dash on Caps Lock, Passive Effect
 * switches on H / N / J / Y, Grab on R (hold to throw) and Release on the grave key. Also the client-side cooldown mirror for the HUD.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID, value = Dist.CLIENT)
public final class ModKeys {
    public static final String CATEGORY = "key.categories.rsm";
    public static final KeyMapping SLASH = new KeyMapping("key.rsm.slash", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, CATEGORY);
    public static final KeyMapping STRIKE = new KeyMapping("key.rsm.strike", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, CATEGORY);
    public static final KeyMapping CLAP = new KeyMapping("key.rsm.clap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping DASH = new KeyMapping("key.rsm.dash", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_CAPS_LOCK, CATEGORY);
    public static final KeyMapping TOGGLE_HASTE = new KeyMapping("key.rsm.toggle_haste", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping TOGGLE_NIGHT_VISION = new KeyMapping("key.rsm.toggle_night_vision", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);
    public static final KeyMapping TOGGLE_REGENERATION = new KeyMapping("key.rsm.toggle_regeneration", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
    public static final KeyMapping TOGGLE_STRENGTH = new KeyMapping("key.rsm.toggle_strength", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, CATEGORY);
    public static final KeyMapping GRAB = new KeyMapping("key.rsm.grab", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping RELEASE = new KeyMapping("key.rsm.release", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, CATEGORY);

    /** Optimistic copy of the server cooldowns, driven by our own key presses. */
    public static final AttackCooldowns COOLDOWNS = new AttackCooldowns();

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SLASH);
        event.register(STRIKE);
        event.register(CLAP);
        event.register(DASH);
        event.register(TOGGLE_HASTE);
        event.register(TOGGLE_NIGHT_VISION);
        event.register(TOGGLE_REGENERATION);
        event.register(TOGGLE_STRENGTH);
        event.register(GRAB);
        event.register(RELEASE);
    }

    public static KeyMapping keyFor(AttackKind kind) {
        return switch (kind) {
            case SLASH -> SLASH;
            case STRIKE -> STRIKE;
            case CLAP -> CLAP;
        };
    }

    public static KeyMapping keyFor(PassiveEffect effect) {
        return switch (effect) {
            case HASTE -> TOGGLE_HASTE;
            case NIGHT_VISION -> TOGGLE_NIGHT_VISION;
            case REGENERATION -> TOGGLE_REGENERATION;
            case STRENGTH -> TOGGLE_STRENGTH;
        };
    }

    /** Called every client tick after the game processed input. */
    static void tick(LocalPlayer player) {
        while (SLASH.consumeClick()) tryAttack(player, AttackKind.SLASH);
        while (STRIKE.consumeClick()) tryAttack(player, AttackKind.STRIKE);
        while (CLAP.consumeClick()) tryAttack(player, AttackKind.CLAP);
        while (DASH.consumeClick()) Dash.tryDash(player);
        for (PassiveEffect effect : PassiveEffect.values()) {
            while (keyFor(effect).consumeClick()) togglePassive(player, effect);
        }
    }

    private static void togglePassive(LocalPlayer player, PassiveEffect effect) {
        if (!Bearer.isBearer(player)) return;
        ClientPassives.toggle(effect);
        PacketDistributor.sendToServer(new TogglePassivePayload(effect));
    }

    private static void tryAttack(LocalPlayer player, AttackKind kind) {
        if (player.isSpectator() || !Bearer.isBearer(player)) return;
        if (kind == AttackKind.CLAP && ClientGrabs.isHolding(player.getId())) return;
        if (!COOLDOWNS.isReady(kind, player.tickCount)) return;
        COOLDOWNS.trigger(kind, player.tickCount, kind.cooldownTicks());
        if (kind == AttackKind.CLAP) {
            ClapAnimation.start(player.getId());
        } else {
            player.swing(InteractionHand.MAIN_HAND);
        }
        PacketDistributor.sendToServer(new AttackPayload(kind));
    }

    private ModKeys() {}
}
