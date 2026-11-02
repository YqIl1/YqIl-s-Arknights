package com.YqIl1.yqils_arknights.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

// Shared "may this area skill hit / lock onto this entity?" rule (Texas's Sword Rain, Typhon's lock-on and
// arrow rain). Never: the caster, spectators, armor stands, or the caster's own creatures. Other players and
// creatures owned by other players: only when PvP allows hurting that player (Player#canHarmPlayer -- server
// PvP setting + team friendly fire). Everything else (wild / unowned mobs): yes.
public final class SkillTargeting {
    private SkillTargeting() {
    }

    public static boolean canSkillHit(Player caster, LivingEntity e) {
        if (e == caster || e.isSpectator() || e instanceof ArmorStand) {
            return false;
        }
        if (e instanceof Player other) {
            return caster.canHarmPlayer(other);
        }
        if (e instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            UUID owner = ownable.getOwnerUUID();
            if (owner.equals(caster.getUUID())) {
                return false; // your own creatures are always immune
            }
            Player ownerPlayer = e.level().getPlayerByUUID(owner);
            if (ownerPlayer != null) {
                return caster.canHarmPlayer(ownerPlayer);
            }
            // Owner offline: fall back to the server's PvP setting.
            return caster.getServer() != null && caster.getServer().isPvpAllowed();
        }
        return true;
    }
}
