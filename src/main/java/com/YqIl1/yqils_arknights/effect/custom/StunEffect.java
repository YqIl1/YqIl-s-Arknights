package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

// Stun: disables the target's AI (movement, targeting, attacking) for the effect's duration via
// Mob#setNoAi. Only affects Mob subclasses -- Player has no AI goals to disable, so this effect
// does nothing to a stunned player beyond whatever else it's combined with.
public class StunEffect extends MobEffect {
    public StunEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // re-pin every tick in case something else clears NoAi mid-effect
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
    }

    // Called once the effect actually ends (duration runs out or it's removed) -- restores AI so the
    // mob doesn't get stuck permanently stunned.
    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }
    }
}
