package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.TimedActivation;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class NearlShieldItem extends ShieldItem implements CrosshairCounterItem, ActivatableSkillItem {
    private static final String FIRST_AID_LAST_USED_KEY = "yqils_arknights:nearl_shield_last_used";
    public static final int FIRST_AID_DURATION_TICKS = 800; // 40s buff
    public static final int FIRST_AID_COOLDOWN_SECONDS = 40; // additional cooldown, starts once the buff ends
    public static final int FIRST_AID_COOLDOWN_TICKS = FIRST_AID_COOLDOWN_SECONDS * 20;
    private static final UUID BASE_ATTACK_DAMAGE_UUID =
            UUID.fromString("2AD3F344-0000-4000-8000-000000000001");
    private static final UUID OFFHAND_BLOCK_BONUS_UUID =
            UUID.fromString("2AD3F344-0000-4000-8000-000000000002");
    private final Multimap<Attribute, AttributeModifier> mainhandModifiers;
    private final Multimap<Attribute, AttributeModifier> offhandModifiers;

    public NearlShieldItem(Properties p_43089_) {
        super(p_43089_.rarity(Rarity.EPIC).durability(870));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> mainBuilder = ImmutableMultimap.builder();
        mainBuilder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID, "Base attack damage", 8.0D, AttributeModifier.Operation.ADDITION));
        this.mainhandModifiers = mainBuilder.build();

        ImmutableMultimap.Builder<Attribute, AttributeModifier> offBuilder = ImmutableMultimap.builder();
        offBuilder.put(Attributes.ARMOR, new AttributeModifier(
                OFFHAND_BLOCK_BONUS_UUID, "Base attack damage", 8.0D, AttributeModifier.Operation.ADDITION));
        this.offhandModifiers = offBuilder.build();
    }

    // Applies whether the shield is held main hand or off hand, unlike the default (mainhand-only)
    // behavior most weapons use.
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.mainhandModifiers;
        } else if (slot == EquipmentSlot.OFFHAND) {
            return this.offhandModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> components, TooltipFlag pIsAdvanced) {
        components.add(Component.translatable("item.yqils_arknights.nearl_shield.description"));
        super.appendHoverText(pStack, pLevel, components, pIsAdvanced);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    ModSound.NEARL_SHIELD_MELEE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack stack = player.getItemInHand(interactionHand);
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, interactionHand);
        }

        if (!level.isClientSide) {
            activateSkill(stack, level, player);
        }

        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(stack);
    }

    public static int getFirstAidElapsedSeconds(ItemStack stack, Level level) {
        return TimedActivation.getElapsedSeconds(stack, level, FIRST_AID_LAST_USED_KEY, FIRST_AID_DURATION_TICKS, FIRST_AID_COOLDOWN_SECONDS);
    }

    @Override
    public String getCrosshairCounterText(ItemStack stack, Level level) {
        return getFirstAidElapsedSeconds(stack, level) + "/" + FIRST_AID_COOLDOWN_SECONDS;
    }

    @Override
    public net.minecraftforge.registries.RegistryObject<net.minecraft.world.effect.MobEffect> getSkillEffect() {
        return ModEffects.NEARL_SHIELD_POWER;
    }

    @Override
    public int getSkillDurationTicks() {
        return FIRST_AID_DURATION_TICKS;
    }

    @Override
    public int getSkillCooldownTicks() {
        return FIRST_AID_COOLDOWN_TICKS;
    }

    @Override
    public String getSkillLastUsedKey() {
        return FIRST_AID_LAST_USED_KEY;
    }

    @Override
    public net.minecraftforge.registries.RegistryObject<net.minecraft.sounds.SoundEvent> getSkillSound() {
        return ModSound.NEARL_SHIELD_POWER;
    }
}
