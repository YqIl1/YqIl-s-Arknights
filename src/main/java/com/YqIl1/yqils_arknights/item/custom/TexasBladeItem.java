package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.entity.custom.TexasBladeProjectile;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.TimedActivation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TexasBladeItem extends SwordItem implements CrosshairCounterItem, ActivatableSkillItem{
    private static final String SWORDRAIN_LAST_USED_KEY = "yqils_arknights:swordrain_last_used";
    private static final String SWORDRAIN_PENDING_STRIKE_KEY = "yqils_arknights:swordrain_pending_strike";
    public static final int SWORDRAIN_DURATION_TICKS = 1; // placeholder -- instant skill until Texas's kit is decided
    public static final int SWORDRAIN_COOLDOWN_SECONDS = 40;
    public static final int SWORDRAIN_COOLDOWN_TICKS = SWORDRAIN_COOLDOWN_SECONDS * 20;

    private static final double STRIKE_RADIUS = 5.0; // blocks
    private static final float STRIKE_DAMAGE_PERCENT = 1.7F; // of the wielder's own ATTACK_DAMAGE
    private static final int STUN_DURATION_TICKS = 60; // 3s
    private static final int REPEAT_DELAY_TICKS = 10; // 0.5s -- second wave hits this many ticks later

    private static final double BLADE_SPAWN_HEIGHT = 6.0; // blocks above the target
    private static final double BLADE_FALL_SPEED = 0.7; // blocks/tick downward

    public TexasBladeItem(Properties properties) {
        super(Tiers.DIAMOND, 4, -2.4F, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> components, TooltipFlag p_41424_) {
        components.add(Component.translatable("item.yqils_arknights.texas_blade.description"));
        super.appendHoverText(p_41421_, p_41422_, components, p_41424_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (level.isClientSide) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (!activateSkill(itemStack, level, player)) {
            return InteractionResultHolder.fail(itemStack);
        }
        return InteractionResultHolder.success(itemStack);
    }

    // Overrides ActivatableSkillItem's default (which would just self-buff the wielder) -- Sword Rain
    // hits nearby mobs instead, so it needs its own targeting logic rather than the shared one.
    @Override
    public boolean activateSkill(ItemStack stack, Level level, Player player) {
        if (level.isClientSide || !TimedActivation.isReady(stack, level, SWORDRAIN_LAST_USED_KEY, SWORDRAIN_DURATION_TICKS, SWORDRAIN_COOLDOWN_TICKS)) {
            return false;
        }

        TimedActivation.markActivated(stack, level, SWORDRAIN_LAST_USED_KEY);
        // This override replaces the interface default, which is where the skill sound normally plays.
        RegistryObject<SoundEvent> sound = getSkillSound();
        if (sound != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), sound.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        strikeNearbyMobs(level, player);
        // The second wave can't happen synchronously (needs to wait 2 ticks), so queue it via NBT and
        // pick it up in inventoryTick below instead.
        stack.getOrCreateTag().putLong(SWORDRAIN_PENDING_STRIKE_KEY, level.getGameTime() + REPEAT_DELAY_TICKS);
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(SWORDRAIN_PENDING_STRIKE_KEY)) {
            return;
        }

        if (level.getGameTime() >= tag.getLong(SWORDRAIN_PENDING_STRIKE_KEY)) {
            tag.remove(SWORDRAIN_PENDING_STRIKE_KEY);
            strikeNearbyMobs(level, player);
        }
    }

    private static void strikeNearbyMobs(Level level, Player player) {
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * STRIKE_DAMAGE_PERCENT;
        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(STRIKE_RADIUS),
                target -> target != player && target.isAlive()
        );

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().indirectMagic(player, player), damage);
            target.addEffect(new MobEffectInstance(ModEffects.STUN.get(), STUN_DURATION_TICKS, 0));
            spawnFallingBlade(level, player, target);
        }
    }

    // Purely cosmetic -- drops a non-rotating sword straight down onto the target from above,
    // landing at roughly the same time the hit/stun already applied above takes effect.
    private static void spawnFallingBlade(Level level, Player player, LivingEntity target) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        TexasBladeProjectile blade = new TexasBladeProjectile(level, player);
        blade.setPos(target.getX(), target.getY() + BLADE_SPAWN_HEIGHT, target.getZ());
        blade.setDeltaMovement(0, -BLADE_FALL_SPEED, 0);
        serverLevel.addFreshEntity(blade);
    }

    @Override
    public RegistryObject<MobEffect> getSkillEffect() {
        return ModEffects.STUN;
    }

    @Override
    public int getSkillDurationTicks() {
        return SWORDRAIN_DURATION_TICKS;
    }

    @Override
    public int getSkillCooldownTicks() {
        return SWORDRAIN_COOLDOWN_TICKS;
    }

    @Override
    public String getSkillLastUsedKey() {
        return SWORDRAIN_LAST_USED_KEY;
    }

    @Override
    public RegistryObject<SoundEvent> getSkillSound() {
        return ModSound.TEXAS_BLADE_POWER;
    }

    // Texas's own swing sound on every melee hit (heard by nearby players), on top of vanilla's.
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    ModSound.TEXAS_BLADE_MELEE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    public static int getSwordrainElapsedSeconds(ItemStack stack, Level level) {
        return TimedActivation.getElapsedSeconds(stack, level, SWORDRAIN_LAST_USED_KEY, SWORDRAIN_DURATION_TICKS, SWORDRAIN_COOLDOWN_SECONDS);
    }

    @Override
    public String getCrosshairCounterText(ItemStack stack, Level level) {
        return getSwordrainElapsedSeconds(stack, level) + "/" + SWORDRAIN_COOLDOWN_SECONDS;
    }
}
