package com.YqIl1.yqils_arknights.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

// PRTS "stay" / "move" for owned mobs. Added at priority 0 with the MOVE flag, so while it runs the mob's
// own movement goals (follow owner, wander, chase) can't -- which also stops tamed animals teleporting back
// to the owner. JUMP is left free so FloatGoal still keeps it from drowning.
//  - MOVE: path to the ordered position; on arrival switch to STAY there (tamed animals sit down instead).
//  - STAY: stand still (walk back if pushed more than 2 blocks away); targets are dropped.
public class PrtsHoldGoal extends Goal {
    private static final double ARRIVE_DIST_SQR = 1.5D * 1.5D;
    private static final double DRIFT_DIST_SQR = 2.0D * 2.0D;
    private static final double SPEED = 1.2D;
    private static final int REPATH_TICKS = 10;

    private final Mob mob;
    private int repath;

    public PrtsHoldGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private boolean active() {
        String order = PrtsOrders.get(mob);
        return PrtsOrders.STAY.equals(order) || PrtsOrders.MOVE.equals(order);
    }

    @Override
    public boolean canUse() {
        return active();
    }

    @Override
    public boolean canContinueToUse() {
        return active();
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
        repath = 0;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        mob.setTarget(null);
        Vec3 pos = PrtsOrders.pos(mob);
        double distSqr = mob.distanceToSqr(pos);
        boolean moving = PrtsOrders.MOVE.equals(PrtsOrders.get(mob));

        if (moving && distSqr <= ARRIVE_DIST_SQR) {
            mob.getNavigation().stop();
            if (mob instanceof TamableAnimal tamable) {
                // Tamed animals hold position the vanilla way (sit), which needs no order at all.
                PrtsOrders.clear(mob);
                tamable.setOrderedToSit(true);
                tamable.setInSittingPose(true);
            } else {
                PrtsOrders.setPosOrder(mob, PrtsOrders.STAY, pos);
            }
            return;
        }

        if (moving || distSqr > DRIFT_DIST_SQR) {
            if (--repath <= 0) {
                repath = REPATH_TICKS;
                mob.getNavigation().moveTo(pos.x, pos.y, pos.z, SPEED);
            }
        } else {
            mob.getNavigation().stop();
        }
    }
}
