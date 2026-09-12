package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.entity.custom.LapplandBladeProjectile;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.sound.ModSound;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LapplandBladeItem extends SwordItem implements CrosshairCounterItem {
    private static final String HIT_COUNTER_KEY = "yqils_arknights:hit_counter";
    public static final int POWER_THRESHOLD = 17;
    public static final int POWER_EFFECT_DURATION_TICKS = 400;

    public LapplandBladeItem() {
        super(Tiers.DIAMOND, 5, -2.4F, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            target.addEffect(new MobEffectInstance(ModEffects.FRAGILE.get(), 160, 0));

            SoundEvent meleeSound = attacker.hasEffect(ModEffects.LAPPLAND_BLADE_POWER.get())
                    ? ModSound.LAPPLAND_BLADE_POWER_MELEE.get()
                    : ModSound.LAPPLAND_BLADE_MELEE.get();
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    meleeSound, SoundSource.PLAYERS, 1.0F, 1.0F);

            registerHit(stack, attacker);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponent, TooltipFlag pIsAdvanced) {
        pTooltipComponent.add(Component.translatable("item.yqils_arknights.lappland_blade.description"));
        super.appendHoverText(pStack, pLevel, pTooltipComponent, pIsAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        double attackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
        if (attackSpeed == 0){
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            // Ranged attacks are normally reduced to 80% of melee damage, but that reduction is waived
            // while Lappland's Power is active
            float rangedDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            boolean hasPower = player.hasEffect(ModEffects.LAPPLAND_BLADE_POWER.get());
            if (!hasPower) {
                rangedDamage *= 0.8F;
            }

            fireProjectile(level,player,rangedDamage,2,0);
            // shoot extra 1 projectile when have effect
            if (hasPower) {
                fireProjectile(level,player,rangedDamage,3-(float)(Math.random()*0.5),3-(float)Math.random()*6);
            }
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));

            SoundEvent rangedSound = hasPower
                    ? ModSound.LAPPLAND_BLADE_POWER_RANGED.get()
                    : ModSound.LAPPLAND_BLADE_RANGED.get();
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    rangedSound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // Cooldown matches however long it currently takes this player to fully recharge their attack (20 ticks / attacks-per-second)

        int cooldownTicks = (int) Math.round(20.0 / attackSpeed);
        player.getCooldowns().addCooldown(this, cooldownTicks);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // Called whenever this specific blade lands a hit (melee or thrown) -- tracks a per-stack combo
    // counter that, on reaching POWER_THRESHOLD, grants a burst of extra attack damage and resets.
    public static void registerHit(ItemStack stack, LivingEntity attacker) {
        if (!(stack.getItem() instanceof LapplandBladeItem)) {
            return;
        }
        if (attacker.hasEffect(ModEffects.LAPPLAND_BLADE_POWER.get())) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int count = tag.getInt(HIT_COUNTER_KEY) + 1;

        if (count >= POWER_THRESHOLD) {
            count = 0;
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    ModSound.LAPPLAND_BLADE_POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            attacker.addEffect(new MobEffectInstance(ModEffects.LAPPLAND_BLADE_POWER.get(), POWER_EFFECT_DURATION_TICKS, 0));
        }

        tag.putInt(HIT_COUNTER_KEY, count);
    }

    private LapplandBladeProjectile fireProjectile(Level level, Player player, float dmg, float xOffset, float yOffset) {
        LapplandBladeProjectile projectile = new LapplandBladeProjectile(level, player);
        projectile.setDamage(dmg);
        projectile.shootFromRotation(player, player.getXRot()-xOffset, player.getYRot()+yOffset, 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(projectile);
        return projectile;
    }

    // Read-only; doesn't create a tag on the stack just from being queried (unlike getOrCreateTag)
    public static int getHitCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(HIT_COUNTER_KEY) : 0;
    }

    @Override
    public String getCrosshairCounterText(ItemStack stack, Level level) {
        return getHitCount(stack) + "/" + POWER_THRESHOLD;
    }
}
