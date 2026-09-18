package dev.ashu.rsm.registry;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.item.DnaRingItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RawSuperhumanMod.MOD_ID);

    public static final DeferredItem<DnaRingItem> RING = ITEMS.registerItem("ring", DnaRingItem::new,
        new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(ModItems::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(RING);
        }
    }

    private ModItems() {}
}
