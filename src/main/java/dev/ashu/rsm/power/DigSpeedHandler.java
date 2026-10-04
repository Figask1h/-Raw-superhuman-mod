package dev.ashu.rsm.power;

import dev.ashu.rsm.RawSuperhumanMod;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * A Bearer digs as fast in the air and under water as standing on dry land. Undoes the two vanilla penalties
 * of Player.getDigSpeed: divided by 5 off the ground, times submerged_mining_speed (0.2 without Aqua Affinity)
 * with the eyes in water. Runs on both sides, so the client's crack progress matches the server.
 */
@EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
public final class DigSpeedHandler {
    /** Vanilla's divisor for a player who is not on the ground. */
    private static final float AIRBORNE_PENALTY = 5.0F;

    @SubscribeEvent
    @SuppressWarnings("deprecation") // the very check Player.getDigSpeed applies the penalty with
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!Bearer.isBearer(player)) return;
        float speed = event.getNewSpeed();
        if (!player.onGround()) speed *= AIRBORNE_PENALTY;
        if (player.isEyeInFluid(FluidTags.WATER)) {
            double submerged = player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
            // Only undo a slowdown: Aqua Affinity already brings it to 1, and 0 would divide by zero.
            if (submerged > 0.0 && submerged < 1.0) speed /= (float) submerged;
        }
        event.setNewSpeed(speed);
    }

    private DigSpeedHandler() {}
}
