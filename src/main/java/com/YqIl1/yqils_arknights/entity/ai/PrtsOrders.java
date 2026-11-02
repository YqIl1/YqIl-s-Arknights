package com.YqIl1.yqils_arknights.entity.ai;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// The standing PRTS order of one owned mob, kept in its persistent data (saved with the entity).
// No order = normal behaviour (follow the owner). Read by PrtsHoldGoal / PrtsAttackTargetGoal.
public final class PrtsOrders {
    public static final String ORDER_KEY = "yqils_arknights:prts_order";
    private static final String X_KEY = "yqils_arknights:prts_x";
    private static final String Y_KEY = "yqils_arknights:prts_y";
    private static final String Z_KEY = "yqils_arknights:prts_z";
    private static final String TARGET_KEY = "yqils_arknights:prts_target";

    public static final String STAY = "stay";     // hold position
    public static final String MOVE = "move";     // walk to a position, then STAY there
    public static final String ATTACK = "attack"; // attack one target until it's gone

    private PrtsOrders() {
    }

    public static String get(Mob mob) {
        return mob.getPersistentData().getString(ORDER_KEY);
    }

    public static void clear(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        data.remove(ORDER_KEY);
        data.remove(X_KEY);
        data.remove(Y_KEY);
        data.remove(Z_KEY);
        data.remove(TARGET_KEY);
    }

    public static void setPosOrder(Mob mob, String order, Vec3 pos) {
        clear(mob);
        CompoundTag data = mob.getPersistentData();
        data.putString(ORDER_KEY, order);
        data.putDouble(X_KEY, pos.x);
        data.putDouble(Y_KEY, pos.y);
        data.putDouble(Z_KEY, pos.z);
    }

    public static void setAttack(Mob mob, LivingEntity target) {
        clear(mob);
        CompoundTag data = mob.getPersistentData();
        data.putString(ORDER_KEY, ATTACK);
        data.putUUID(TARGET_KEY, target.getUUID());
    }

    public static Vec3 pos(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        return new Vec3(data.getDouble(X_KEY), data.getDouble(Y_KEY), data.getDouble(Z_KEY));
    }

    // The ordered attack target if it still exists and is alive, else null.
    @Nullable
    public static LivingEntity target(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        if (!data.hasUUID(TARGET_KEY) || !(mob.level() instanceof ServerLevel level)) return null;
        Entity e = level.getEntity(data.getUUID(TARGET_KEY));
        return e instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    public static UUID targetId(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        return data.hasUUID(TARGET_KEY) ? data.getUUID(TARGET_KEY) : null;
    }
}
