package dev.ashu.rsm;

import dev.ashu.rsm.registry.ModAttachments;
import dev.ashu.rsm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(RawSuperhumanMod.MOD_ID)
public final class RawSuperhumanMod {
    public static final String MOD_ID = "rsm";

    public RawSuperhumanMod(IEventBus modBus, ModContainer container) {
        ModItems.register(modBus);
        ModAttachments.register(modBus);

        // Gameplay numbers are a SERVER config: it lives with the world and NeoForge syncs it
        // to clients on login, so client-side flight math uses the same values as the server.
        container.registerConfig(ModConfig.Type.SERVER, RsmConfig.SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, RsmConfig.CLIENT_SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
