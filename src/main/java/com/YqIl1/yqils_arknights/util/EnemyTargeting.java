package com.YqIl1.yqils_arknights.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// "Is this an enemy of `viewer`?" for auto-aim and enemy counting (Eyjafjalla's Volcano homing, Mlynar's
// enemy count). An enemy is a living, non-spectator creature that is
//   - an Enemy (vanilla hostiles), OR in the #yqils_arknights:skill_targets tag, OR from Torches Become
//     Sunlight, OR a mob currently targeting the viewer,
//   - AND not in #yqils_arknights:skill_ignored,
//   - AND (viewer is a player) allowed by SkillTargeting: never your own creatures, others' only with PvP.
// Note: Typhon's lock-on deliberately does NOT use this -- it may lock any creature SkillTargeting allows.
public final class EnemyTargeting {
    private static final String TORCHES_BECOME_SUNLIGHT_MODID = "torchesbecomesunlight";

    private EnemyTargeting() {
    }

    public static boolean isEnemyOf(LivingEntity viewer, LivingEntity e) {
        if (e == viewer || !e.isAlive() || e.isSpectator() || e.getType().is(ModTags.EntityTypes.SKILL_IGNORED)) {
            return false;
        }
        if (viewer instanceof Player player && !SkillTargeting.canSkillHit(player, e)) {
            return false;
        }
        if (e instanceof Enemy || e.getType().is(ModTags.EntityTypes.SKILL_TARGETS)) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
        if (id != null && TORCHES_BECOME_SUNLIGHT_MODID.equals(id.getNamespace())) {
            return true;
        }
        return e instanceof Mob mob && mob.getTarget() == viewer;
    }

    public static List<LivingEntity> enemiesNear(LivingEntity viewer, AABB area) {
        return viewer.level().getEntitiesOfClass(LivingEntity.class, area, e -> isEnemyOf(viewer, e));
    }

    // A random enemy of `viewer` within `radius` of the centre of `area`, or null.
    @Nullable
    public static LivingEntity randomEnemyNear(LivingEntity viewer, net.minecraft.world.phys.Vec3 center, double radius) {
        List<LivingEntity> list = enemiesNear(viewer, new AABB(center, center).inflate(radius));
        list.removeIf(e -> e.distanceToSqr(center) > radius * radius);
        return list.isEmpty() ? null : list.get(viewer.getRandom().nextInt(list.size()));
    }
}
