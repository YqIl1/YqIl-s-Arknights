package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class ImmolationEffect extends MobEffect {
    // Attack damage (and attribution for the damage source) belongs to whoever applied the effect,
    // not the burning target -- so the source writes a snapshot of 33% of their attack damage plus
    // their UUID here (on the target) at application time, and every tick just reads both back.
    public static final String SOURCE_DAMAGE_KEY = "yqils_arknights:immolation_source_damage";
    public static final String SOURCE_UUID_KEY = "yqils_arknights:immolation_source_uuid";
    private static final float DEFAULT_DAMAGE_PER_TICK = 1.0F;

    public ImmolationEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.ARMOR, "de5f830c-77e3-4bc7-83d0-dbbb5fc8aa73", -3.0D, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0; // once per second
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        float damage = entity.getPersistentData().contains(SOURCE_DAMAGE_KEY)
                ? entity.getPersistentData().getFloat(SOURCE_DAMAGE_KEY)
                : DEFAULT_DAMAGE_PER_TICK;

        Entity source = entity;
        if (entity.level() instanceof ServerLevel serverLevel && entity.getPersistentData().hasUUID(SOURCE_UUID_KEY)) {
            UUID sourceUuid = entity.getPersistentData().getUUID(SOURCE_UUID_KEY);
            Entity found = serverLevel.getEntity(sourceUuid);
            if (found != null) {
                source = found;
            }
        }

        entity.hurt(entity.damageSources().indirectMagic(source, source), damage);
    }
}
