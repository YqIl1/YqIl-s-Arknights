package com.YqIl1.yqils_arknights.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// Weapon magic damage. It *is* vanilla indirect magic (same damage type, so: ignores armor; still reduced by
// Protection, Resistance, witches' magic resistance and any mod that resists vanilla magic) -- only the death
// message changes, to name the weapon: translatable(messageKey, victim, attacker, weapon).
//   magic(attacker, weapon)                    direct hit (e.g. Texas's Sword Rain), generic message
//   magic(projectile, attacker, weapon)        from a projectile (Eyjafjalla's wand, powered Lappland blade)
//   ...withMessage(key)                        a skill-specific message, e.g. SWORD_RAIN
public class WeaponMagicDamageSource extends DamageSource {
    public static final String GENERIC = "death.attack.yqils_arknights.weapon_magic";
    public static final String SWORD_RAIN = "death.attack.yqils_arknights.sword_rain";

    private final ItemStack weapon;
    private final String messageKey;

    private WeaponMagicDamageSource(Entity direct, @Nullable Entity attacker, ItemStack weapon, String messageKey) {
        super(direct.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.INDIRECT_MAGIC),
                direct, attacker);
        this.weapon = weapon.copy();
        this.messageKey = messageKey;
    }

    public static WeaponMagicDamageSource magic(Entity attacker, ItemStack weapon) {
        return new WeaponMagicDamageSource(attacker, attacker, weapon, GENERIC);
    }

    public static WeaponMagicDamageSource magic(Entity projectile, @Nullable Entity attacker, ItemStack weapon) {
        return new WeaponMagicDamageSource(projectile, attacker, weapon, GENERIC);
    }

    public WeaponMagicDamageSource withMessage(String key) {
        return new WeaponMagicDamageSource(this.getDirectEntity(), this.getEntity(), this.weapon, key);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity victim) {
        Entity attacker = this.getEntity();
        if (attacker == null || weapon.isEmpty()) {
            return super.getLocalizedDeathMessage(victim); // vanilla "killed by magic" wording
        }
        return Component.translatable(messageKey, victim.getDisplayName(), attacker.getDisplayName(), weapon.getDisplayName());
    }
}
