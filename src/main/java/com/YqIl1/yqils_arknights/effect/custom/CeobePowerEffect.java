package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// "很重的枪" / Really Heavy Spear -- granted by Ceobe's Spear's right-click skill (60 s).
//  - ATK +210% (attribute modifier below; MULTIPLY_BASE 2.1 -> base * 3.1)
//  - attacks silence the target for 5 s (CeobeSpearItem#hurtEnemy and ThrownCeobeSpear#onHitEntity)
//  - the spear can be thrown while this is active (CeobeSpearItem#use / releaseUsing)
public class CeobePowerEffect extends MobEffect {
    public CeobePowerEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "248a2c6e-49e2-4fed-9dad-34648a6f1d05", 2.1D, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
