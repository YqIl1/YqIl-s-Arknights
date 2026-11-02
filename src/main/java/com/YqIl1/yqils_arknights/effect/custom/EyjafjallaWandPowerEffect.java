package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class EyjafjallaWandPowerEffect extends MobEffect {
    public EyjafjallaWandPowerEffect(MobEffectCategory category, int color) {
        super(category, color);
        // Volcano: ATK +130% (MULTIPLY_BASE 1.3 -> base * 2.3). Shot behaviour lives in EyjafjallaWandItem#use.
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "549a117c-6381-487b-9459-3f2cc35cb0af", 1.3D, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
