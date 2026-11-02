package com.YqIl1.yqils_arknights.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

// The attack speed our attack-speed-scaled timings use (Typhon draw, Ceobe throw charge, Eyjafjalla shot
// cooldown, Ifrit hit interval). Clamped to [MIN, MAX] so mods that push ATTACK_SPEED to extreme values
// (e.g. "no attack cooldown" mods setting it to 1024) can't make these fire every tick.
public final class SkillAttackSpeed {
    public static final double MIN = 0.1D;
    public static final double MAX = 4.0D;

    private SkillAttackSpeed() {
    }

    public static double of(LivingEntity entity) {
        double speed = entity.getAttributeValue(Attributes.ATTACK_SPEED);
        return Math.max(MIN, Math.min(MAX, speed));
    }
}
