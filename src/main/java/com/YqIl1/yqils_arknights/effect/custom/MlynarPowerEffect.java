package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class MlynarPowerEffect extends MobEffect {
    private static final UUID ENEMY_BOOST_MODIFIER_ID = UUID.fromString("8a756e1f-0539-46f5-ba96-4d2195aff3ef");
    public static final String KILL_COUNT_KEY = "yqils_arknights:mlynar_kill_count";
    public static final String THRESHOLD_MET_KEY = "yqils_arknights:mlynar_threshold_met";
    public static final float THRESHOLD_DAMAGE_REDUCTION = 0.25F;

    private static final double BASE_BOOST_PERCENT = 400.0D;
    private static final double MATCHED_BOOST_PERCENT = 405.0D;
    private static final double DECAY_PERCENT_PER_KILL = 10.0D;
    private static final int MAX_DECAY_STACKS = 20;

    private static final double ENEMY_BOOST_RADIUS = 5.0D;
    private static final int ENEMY_BOOST_THRESHOLD = 3;

    public MlynarPowerEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(ForgeMod.BLOCK_REACH.get(), "cbf67f10-4aa2-4ed7-bc88-39228b6b9aa4", 2.0D, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(ForgeMod.ENTITY_REACH.get(), "e9342a1f-5426-4266-8d8a-d25df3ab1b56", 2.0D, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }

        long nearbyEnemies = entity.level().getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(ENEMY_BOOST_RADIUS),
                e -> e instanceof Enemy && e.isAlive()
        ).size();

        boolean thresholdMet = nearbyEnemies >= ENEMY_BOOST_THRESHOLD;
        entity.getPersistentData().putBoolean(THRESHOLD_MET_KEY, thresholdMet);

        int kills = Math.min(entity.getPersistentData().getInt(KILL_COUNT_KEY), MAX_DECAY_STACKS);
        double basePercent = thresholdMet ? MATCHED_BOOST_PERCENT : BASE_BOOST_PERCENT;
        double effectivePercent = basePercent - DECAY_PERCENT_PER_KILL * kills;
        double amount = effectivePercent / 100.0D;

        AttributeModifier current = attackDamage.getModifier(ENEMY_BOOST_MODIFIER_ID);
        if (current == null || current.getAmount() != amount) {
            if (current != null) {
                attackDamage.removeModifier(ENEMY_BOOST_MODIFIER_ID);
            }
            attackDamage.addTransientModifier(new AttributeModifier(
                    ENEMY_BOOST_MODIFIER_ID, "Unbrilliant Glory (enemy boost)", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        AttributeInstance attackDamage = attributeMap.getInstance(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ENEMY_BOOST_MODIFIER_ID);
        }
    }
}
