package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NCDeerFanPowerEffect extends MobEffect {
    // Damage reduction is applied in ModEventHandlers#onLivingHurt (covers all damage types, not
    // just melee), since MobEffect itself has no generic "incoming damage" hook.
    public static final float DAMAGE_REDUCTION = 0.26F;

    private static final String ATTACK_SPEED_MODIFIER_ID = "4a6746ed-ae9f-45ad-9424-b5d2dc9f04fe";

    public NCDeerFanPowerEffect(MobEffectCategory category, int color) {
        super(category, color);
        // Attacks land 1.5x as fast (interval becomes 1/1.5 of before) -- MULTIPLY_TOTAL multiplies
        // the running attribute total by (1 + amount), so 0.5 here means x1.5 overall.
        this.addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_ID, 0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
