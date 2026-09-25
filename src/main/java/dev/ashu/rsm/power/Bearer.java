package dev.ashu.rsm.power;

import dev.ashu.rsm.item.ToggleableItem;
import dev.ashu.rsm.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * A Bearer is a player currently wearing the DNA Alteration Ring in a Curios slot, while the ring is
 * switched on in the common config.
 * Every ability of the mod exists only while {@link #isBearer} is true.
 */
public final class Bearer {

    public static boolean isBearer(LivingEntity entity) {
        if (!(entity instanceof Player) || !ToggleableItem.RING.isEnabled(entity.level())) return false;
        return CuriosApi.getCuriosInventory(entity)
            .map(inventory -> inventory.isEquipped(ModItems.RING.get()))
            .orElse(false);
    }

    private Bearer() {}
}
