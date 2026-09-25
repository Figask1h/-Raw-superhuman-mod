package dev.ashu.rsm.registry;

import com.mojang.serialization.MapCodec;
import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.item.DnaRingItem;
import dev.ashu.rsm.item.ItemEnabledCondition;
import dev.ashu.rsm.item.ToggleableItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RawSuperhumanMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RawSuperhumanMod.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
        DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, RawSuperhumanMod.MOD_ID);

    public static final DeferredItem<DnaRingItem> RING = ITEMS.registerItem("ring", DnaRingItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

    /** Tab contents are built client-side when the creative inventory opens, after the server's item switches arrived. */
    public static final Supplier<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.rsm"))
        .icon(() -> new ItemStack(RING.get()))
        .displayItems((parameters, output) -> {
            if (ToggleableItem.RING.isEnabledOnClient()) output.accept(RING.get());
        })
        .build());

    public static final Supplier<MapCodec<ItemEnabledCondition>> ITEM_ENABLED = CONDITIONS.register("item_enabled",
        () -> ItemEnabledCondition.CODEC);

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
        CONDITIONS.register(modBus);
    }

    private ModItems() {}
}
