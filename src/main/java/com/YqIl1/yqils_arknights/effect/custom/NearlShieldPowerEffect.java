package com.YqIl1.yqils_arknights.effect.custom;

import com.YqIl1.yqils_arknights.sound.ModSound;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NearlShieldPowerEffect extends MobEffect {
    private static final float HEAL_PERCENT = 0.33F; // of the entity's own ATTACK_DAMAGE
    private static final int HEAL_INTERVAL_TICKS = 50; // 2.5s
    private static final int HEAL_WARNING_LEAD_TICKS = 12; // 0.6s before each heal

    public NearlShieldPowerEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int phase = duration % HEAL_INTERVAL_TICKS;
        return phase == 0 || phase == HEAL_WARNING_LEAD_TICKS;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance instance = entity.getEffect(this);
        int duration = instance != null ? instance.getDuration() : 0;

        if (duration % HEAL_INTERVAL_TICKS == 0) {
            float heal = (float) entity.getAttributeValue(Attributes.ATTACK_DAMAGE) * HEAL_PERCENT;
            entity.heal(heal);
        } else if (!entity.level().isClientSide) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSound.NEARL_SHIELD_POWER_HEAL.get(), SoundSource.PLAYERS, 2F, 1.0F);
        }
    }
}
