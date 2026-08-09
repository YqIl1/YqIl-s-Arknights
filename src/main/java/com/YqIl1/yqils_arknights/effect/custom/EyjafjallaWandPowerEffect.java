package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class EyjafjallaWandPowerEffect extends MobEffect {
    public EyjafjallaWandPowerEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "8c2f0d73-a409-4d13-a98e-d1804a6d7dd7", 1.3D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    }
}
