package dev.ashu.rsm.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import dev.ashu.rsm.power.PassiveEffect;
import dev.ashu.rsm.power.PassiveEffects;
import dev.ashu.rsm.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForgeMod;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * The DNA Alteration Ring. Wearing it in a Curios ring slot makes the player a Bearer.
 * The item itself carries no state; every ability checks {@link dev.ashu.rsm.power.Bearer}.
 */
public class DnaRingItem extends Item implements ICurioItem {

    public DnaRingItem(Properties properties) {
        super(properties);
    }

    /** Keeps the Passive Effects the wearer has switched on. */
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (wearer.level().isClientSide || !ToggleableItem.RING.isEnabled(wearer.level())) return;
        PassiveEffects switches = wearer.getData(ModAttachments.PASSIVES);
        for (PassiveEffect effect : PassiveEffect.values()) {
            if (switches.isEnabled(effect)) effect.apply(wearer);
        }
    }

    /** Passive Effects end with the ring instead of running out their last seconds. */
    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (wearer.level().isClientSide || newStack.is(this)) return;
        for (PassiveEffect effect : PassiveEffect.values()) {
            effect.remove(wearer);
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD.value(), 1.0F, 1.0F);
    }

    /** Creative-style flight while worn: NeoForge revokes it automatically when the modifier disappears. */
    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();
        LivingEntity wearer = slotContext.entity();
        boolean enabled = wearer != null ? ToggleableItem.RING.isEnabled(wearer.level()) : ToggleableItem.RING.isEnabledOnClient();
        if (!enabled) return modifiers;
        modifiers.put(NeoForgeMod.CREATIVE_FLIGHT, new AttributeModifier(id, 1.0, AttributeModifier.Operation.ADD_VALUE));
        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.rsm.ring.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!ToggleableItem.RING.isEnabledOnClient()) {
            tooltip.add(Component.translatable("item.rsm.disabled").withStyle(ChatFormatting.RED));
        }
    }
}
