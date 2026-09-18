package dev.ashu.rsm.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.attack.AttackCooldowns;
import dev.ashu.rsm.attack.AttackKind;
import dev.ashu.rsm.network.AttackPayload;
import dev.ashu.rsm.power.Bearer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Attack key bindings (mouse 4 / mouse 5 by default, rebindable in Controls) and the client-side cooldown mirror for the HUD. */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID, value = Dist.CLIENT)
public final class ModKeys {
    public static final String CATEGORY = "key.categories.rsm";
    public static final KeyMapping SLASH = new KeyMapping("key.rsm.slash", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_4, CATEGORY);
    public static final KeyMapping STRIKE = new KeyMapping("key.rsm.strike", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, CATEGORY);
    public static final KeyMapping DASH = new KeyMapping("key.rsm.dash", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_CAPS_LOCK, CATEGORY);

    /** Optimistic copy of the server cooldowns, driven by our own key presses. */
    public static final AttackCooldowns COOLDOWNS = new AttackCooldowns();

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SLASH);
        event.register(STRIKE);
        event.register(DASH);
    }

    public static KeyMapping keyFor(AttackKind kind) {
        return kind == AttackKind.SLASH ? SLASH : STRIKE;
    }

    /** Called every client tick after the game processed input. */
    static void tick(LocalPlayer player) {
        while (SLASH.consumeClick()) tryAttack(player, AttackKind.SLASH);
        while (STRIKE.consumeClick()) tryAttack(player, AttackKind.STRIKE);
        while (DASH.consumeClick()) Dash.tryDash(player);
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
