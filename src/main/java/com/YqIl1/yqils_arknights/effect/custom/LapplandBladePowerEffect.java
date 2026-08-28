package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LapplandBladePowerEffect extends MobEffect {
    public LapplandBladePowerEffect(MobEffectCategory category, int color) {
        super(category, color);
        // MULTIPLY_TOTAL with 1.2 means final = base * (1 + 1.2) -- i.e. attack damage increased by 120%
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "94d0fe0c-7efb-4c5f-a9e3-2d5c51ecd154", 1.2D, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
