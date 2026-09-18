package dev.ashu.rsm.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
    private static final int HASTE_AMPLIFIER = 2;
    private static final int HASTE_DURATION_TICKS = 60;
    private static final int HASTE_REFRESH_TICKS = 20;

    public DnaRingItem(Properties properties) {
        super(properties);
    }

    /** Haste III while worn: refreshed every second so it never flickers and never lingers after removal. */
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (wearer.level().isClientSide) return;
        MobEffectInstance current = wearer.getEffect(MobEffects.DIG_SPEED);
        if (current == null || current.getAmplifier() < HASTE_AMPLIFIER || current.getDuration() <= HASTE_REFRESH_TICKS) {
            wearer.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, HASTE_DURATION_TICKS, HASTE_AMPLIFIER, true, false, true));
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
        modifiers.put(NeoForgeMod.CREATIVE_FLIGHT, new AttributeModifier(id, 1.0, AttributeModifier.Operation.ADD_VALUE));
        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.rsm.ring.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
