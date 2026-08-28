package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.entity.custom.EyjafjallaWandProjectile;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.TimedActivation;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EyjafjallaWandItem extends Item implements CrosshairCounterItem {
    private static final String VOLCANO_LAST_USED_KEY = "yqils_arknights:volcano_last_used";
    public static final int VOLCANO_DURATION_TICKS = 300; // 15s buff
    public static final int VOLCANO_COOLDOWN_TICKS = 1600; // 80s, starts once the buff ends
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public EyjafjallaWandItem(Properties p_41383_) {
        super(p_41383_.rarity(Rarity.EPIC).stacksTo(1).durability(512));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Base attack damage", 7.0D, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }



    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }
    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> components, TooltipFlag p_41424_) {
        components.add(Component.translatable("item.yqils_arknights.eyjafjalla_wand.decription"));
        super.appendHoverText(p_41421_, p_41422_, components, p_41424_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        boolean hasPower = player.hasEffect(ModEffects.EYJAFJALLA_WAND_POWER.get());

        int cooldown = 8;
        if (!level.isClientSide) {
            if (player.isShiftKeyDown() && TimedActivation.isReady(itemstack, level, VOLCANO_LAST_USED_KEY, VOLCANO_DURATION_TICKS, VOLCANO_COOLDOWN_TICKS)) {
                player.addEffect(new MobEffectInstance(ModEffects.EYJAFJALLA_WAND_POWER.get(), VOLCANO_DURATION_TICKS, 0));
                TimedActivation.markActivated(itemstack, level, VOLCANO_LAST_USED_KEY);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSound.EYJAFJALLA_WAND_POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            float rangedDamage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            if (hasPower) {
                rangedDamage /= 2;
                cooldown = 2;
                for(int i = 0; i < 4; i++){
                    fireProjectile(level,player,rangedDamage,3+(float)Math.random()*6,45-(float)Math.random()*90,90);
                }
            }
            fireProjectile(level,player,rangedDamage,2,0,60);

            itemstack.hurtAndBreak(1, player, (entity) -> entity.broadcastBreakEvent(interactionHand));

            level.playSound(null,player.getX(), player.getY(), player.getZ(),
                    ModSound.EYJAFJALLA_WAND_RANGED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);


        }

        player.getCooldowns().addCooldown(this, 2*cooldown);


        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    public static final int VOLCANO_COOLDOWN_SECONDS = VOLCANO_COOLDOWN_TICKS / 20;

    public static int getVolcanoElapsedSeconds(ItemStack stack, Level level) {
        return TimedActivation.getElapsedSeconds(stack, level, VOLCANO_LAST_USED_KEY, VOLCANO_DURATION_TICKS, VOLCANO_COOLDOWN_SECONDS);
    }

    @Override
    public String getCrosshairCounterText(ItemStack stack, Level level) {
        return getVolcanoElapsedSeconds(stack, level) + "/" + VOLCANO_COOLDOWN_SECONDS;
    }

    private EyjafjallaWandProjectile fireProjectile(Level level, Player player, float dmg, float xOffset, float yOffset, int lifetime) {
        EyjafjallaWandProjectile projectile = new EyjafjallaWandProjectile(level, player);
        projectile.setMaxLifetime(lifetime);
        projectile.setDamage(dmg);
        projectile.shootFromRotation(player, player.getXRot()-xOffset, player.getYRot()+yOffset, 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(projectile);

        return projectile;
    }
}
