package dev.ashu.rsm.power;

import dev.ashu.rsm.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * A Bearer is a player currently wearing the DNA Alteration Ring in a Curios slot.
 * Every ability of the mod exists only while {@link #isBearer} is true.
 */
public final class Bearer {

    public static boolean isBearer(LivingEntity entity) {
        if (!(entity instanceof Player)) return false;
        return CuriosApi.getCuriosInventory(entity)
            .map(inventory -> inventory.isEquipped(ModItems.RING.get()))
            .orElse(false);
    }

    private Bearer() {}
}
