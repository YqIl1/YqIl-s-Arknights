package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

public class MlynarPowerEffect extends MobEffect {
    public MlynarPowerEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "8a756e1f-0539-46f5-ba96-4d2195aff3ef", 11.0D, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(ForgeMod.BLOCK_REACH.get(), "cbf67f10-4aa2-4ed7-bc88-39228b6b9aa4", 2.0D, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(ForgeMod.ENTITY_REACH.get(), "e9342a1f-5426-4266-8d8a-d25df3ab1b56", 2.0D, AttributeModifier.Operation.ADDITION);
    }
}
