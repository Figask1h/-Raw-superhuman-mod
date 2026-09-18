package dev.ashu.rsm.registry;

import dev.ashu.rsm.RawSuperhumanMod;
import dev.ashu.rsm.attack.AttackCooldowns;
import dev.ashu.rsm.power.FlightState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RawSuperhumanMod.MOD_ID);

    public static final Supplier<AttachmentType<FlightState>> FLIGHT = ATTACHMENTS.register("flight",
        () -> AttachmentType.builder(FlightState::new).build());

    public static final Supplier<AttachmentType<AttackCooldowns>> ATTACKS = ATTACHMENTS.register("attacks",
        () -> AttachmentType.builder(AttackCooldowns::new).build());

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }

    private ModAttachments() {}
}
