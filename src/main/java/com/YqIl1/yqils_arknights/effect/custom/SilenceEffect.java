package com.YqIl1.yqils_arknights.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

// Silence: the affected creature loses its special abilities for the duration and can only use plain melee.
// This class is only a marker -- everything is enforced in ModEventHandlers, because Minecraft has no single
// "special ability" switch:
//  - all beneficial effects are stripped every tick, and new ones are denied
//  - any damage it deals that isn't its own plain melee hit is cancelled (projectiles, guardian laser,
//    warden sonic boom, evoker fangs, explosions...)
//  - harmful effects its melee would add (wither skeleton, cave spider, husk...) are denied
//  - it can't spawn projectiles, evoker fangs, vexes or effect clouds
//  - endermen / shulkers can't teleport; creepers lose their target, so they can't fuse
//  - slimes / magma cubes don't split into smaller ones when they die
// Stripping effects can't happen in applyEffectTick: LivingEntity.tickEffects is iterating the effect map
// while it calls that, so removing entries there would throw ConcurrentModificationException.
public class SilenceEffect extends MobEffect {
    public SilenceEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
