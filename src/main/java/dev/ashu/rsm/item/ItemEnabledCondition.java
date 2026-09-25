package dev.ashu.rsm.item;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Datapack condition {@code {"type": "rsm:item_enabled", "item": "ring"}}: keeps a recipe or advancement
 * only while that item is switched on in the common config.
 */
public record ItemEnabledCondition(ToggleableItem item) implements ICondition {
    public static final MapCodec<ItemEnabledCondition> CODEC =
        ToggleableItem.CODEC.fieldOf("item").xmap(ItemEnabledCondition::new, ItemEnabledCondition::item);

    @Override
    public boolean test(IContext context) {
        return item.isEnabledInConfig();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
