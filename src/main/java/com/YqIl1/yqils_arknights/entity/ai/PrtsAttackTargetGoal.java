package com.YqIl1.yqils_arknights.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

// PRTS "attack": keeps the mob's target pinned to the ordered entity (targetSelector, priority 0) until that
// entity dies or disappears, then clears the order so the mob goes back to following its owner.
// Actually reaching and hitting it is left to the mob's own attack goals (melee, ranged...).
public class PrtsAttackTargetGoal extends Goal {
    private final Mob mob;
    private LivingEntity target;

    public PrtsAttackTargetGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!PrtsOrders.ATTACK.equals(PrtsOrders.get(mob))) return false;
        this.target = PrtsOrders.target(mob);
        if (this.target == null) {
            PrtsOrders.clear(mob); // target gone (dead / unloaded) -> back to normal
        }
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return PrtsOrders.ATTACK.equals(PrtsOrders.get(mob)) && target != null && target.isAlive()
                && target.getUUID().equals(PrtsOrders.targetId(mob));
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        mob.setTarget(target);
    }

    @Override
    public void tick() {
        if (mob.getTarget() != target) {
            mob.setTarget(target); // some AIs drop or swap targets on their own; keep it pinned
        }
    }

    @Override
    public void stop() {
        if (target == null || !target.isAlive()) {
            if (PrtsOrders.ATTACK.equals(PrtsOrders.get(mob))) PrtsOrders.clear(mob);
            if (mob.getTarget() == target) mob.setTarget(null);
        }
        target = null;
    }
}
