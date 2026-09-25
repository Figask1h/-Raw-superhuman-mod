package dev.ashu.rsm.item;

import com.mojang.serialization.Codec;
import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.RsmConfig;
import dev.ashu.rsm.network.EnabledItemsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Items the common config can switch off. A switched-off item has no recipe and no creative tab entry,
 * and existing copies do nothing. The server decides: clients use the switches it sent on login, so a
 * ring switched off on the server is dead on every client whatever their own config says.
 */
public enum ToggleableItem implements StringRepresentable {
    RING("ring");

    public static final Codec<ToggleableItem> CODEC = StringRepresentable.fromEnum(ToggleableItem::values);

    /** Switched-off items (bit = ordinal) as last told by the server we are connected to. */
    private static int syncedDisabledMask;

    private final String name;

    ToggleableItem(String name) {
        this.name = name;
    }

    private ModConfigSpec.BooleanValue config() {
        return switch (this) {
            case RING -> RsmConfig.RING_ENABLED;
        };
    }

    /** The switch that applies on this side: the server's config, or on a client what the server sent. */
    public boolean isEnabled(Level level) {
        return level.isClientSide ? isEnabledOnClient() : isEnabledInConfig();
    }

    /** This game's own config; recipes are filtered with it on the server. */
    public boolean isEnabledInConfig() {
        return config().get();
    }

    public boolean isEnabledOnClient() {
        return (syncedDisabledMask & (1 << ordinal())) == 0;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** Called by the payload handler, which common code registers: keep this class free of client-only types. */
    public static void setSynced(int disabledMask) {
        syncedDisabledMask = disabledMask;
    }

    public static void resetSynced() {
        syncedDisabledMask = 0;
    }

    @EventBusSubscriber(modid = RawSuperhumanMod.MOD_ID)
    static final class Sync {
        @SubscribeEvent
        static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            int mask = 0;
            for (ToggleableItem item : values()) {
                if (!item.isEnabledInConfig()) mask |= 1 << item.ordinal();
            }
            PacketDistributor.sendToPlayer(player, new EnabledItemsPayload(mask));
        }
    }
}
