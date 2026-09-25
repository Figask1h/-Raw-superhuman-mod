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
 * Key bindings (all rebindable in Controls): attacks on mouse 4 / mouse 5, Dash on Caps Lock, Passive Effect
 * switches on H / N. Also the client-side cooldown mirror for the HUD.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID, value = Dist.CLIENT)
public final class ModKeys {
    public static final String CATEGORY = "key.categories.rsm";
    public static final KeyMapping SLASH = new KeyMapping("key.rsm.slash", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, CATEGORY);
    public static final KeyMapping STRIKE = new KeyMapping("key.rsm.strike", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, CATEGORY);
    public static final KeyMapping DASH = new KeyMapping("key.rsm.dash", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_CAPS_LOCK, CATEGORY);
    public static final KeyMapping TOGGLE_HASTE = new KeyMapping("key.rsm.toggle_haste", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping TOGGLE_NIGHT_VISION = new KeyMapping("key.rsm.toggle_night_vision", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);

    /** Optimistic copy of the server cooldowns, driven by our own key presses. */
    public static final AttackCooldowns COOLDOWNS = new AttackCooldowns();

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SLASH);
        event.register(STRIKE);
        event.register(DASH);
        event.register(TOGGLE_HASTE);
        event.register(TOGGLE_NIGHT_VISION);
    }

    public static KeyMapping keyFor(AttackKind kind) {
        return kind == AttackKind.SLASH ? SLASH : STRIKE;
    }

    /** Called every client tick after the game processed input. */
    static void tick(LocalPlayer player) {
        while (SLASH.consumeClick()) tryAttack(player, AttackKind.SLASH);
        while (STRIKE.consumeClick()) tryAttack(player, AttackKind.STRIKE);
        while (DASH.consumeClick()) Dash.tryDash(player);
        while (TOGGLE_HASTE.consumeClick()) togglePassive(player, PassiveEffect.HASTE);
        while (TOGGLE_NIGHT_VISION.consumeClick()) togglePassive(player, PassiveEffect.NIGHT_VISION);
    }

    private static void togglePassive(LocalPlayer player, PassiveEffect effect) {
        if (!Bearer.isBearer(player)) return;
        ClientPassives.toggle(effect);
        PacketDistributor.sendToServer(new TogglePassivePayload(effect));
    }

    private static void tryAttack(LocalPlayer player, AttackKind kind) {
        if (player.isSpectator() || !Bearer.isBearer(player)) return;
        if (!COOLDOWNS.isReady(kind, player.tickCount)) return;
        COOLDOWNS.trigger(kind, player.tickCount, kind.cooldownTicks());
        player.swing(InteractionHand.MAIN_HAND);
        PacketDistributor.sendToServer(new AttackPayload(kind));
    }

    private ModKeys() {}
}
