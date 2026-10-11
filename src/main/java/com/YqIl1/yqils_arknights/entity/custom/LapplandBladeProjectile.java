package com.YqIl1.yqils_arknights.entity.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.entity.ModEntities;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.item.custom.LapplandBladeItem;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class LapplandBladeProjectile extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME_TICKS = 60; // 3 seconds

    private float damage = 6.0F;

    public LapplandBladeProjectile(EntityType<? extends LapplandBladeProjectile> type, Level level) {
        super(type, level);
    }

    public LapplandBladeProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.LAPPLAND_BLADE_PROJECTILE.get(), shooter, level);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.LAPPLAND_BLADE_PROJECTILE.get();
    }

    @Override
    protected float getGravity() {
        return 0.01F; // default ThrowableProjectile gravity is 0.03F; lower means a flatter, faster-feeling arc
    }

    @Override
    public void tick() {
        super.tick();
        level().addParticle(ParticleTypes.ASH, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        // tickCount is Entity's own built-in age counter, incremented automatically every tick -- despawn
        // as a safety net in case it never hits anything (e.g. flies off into the void)
        if (!this.level().isClientSide && this.tickCount >= MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity projectileOwner = this.getOwner();
        boolean hasPower = projectileOwner instanceof LivingEntity livingOwner && livingOwner.hasEffect(ModEffects.LAPPLAND_BLADE_POWER.get());
        DamageSource damageSource = hasPower
                ? this.damageSources().indirectMagic(this, projectileOwner)
                : this.damageSources().thrown(this, projectileOwner);
        target.hurt(damageSource, this.damage);

        if (!this.level().isClientSide) {
            if (target instanceof LivingEntity livingTarget) {
                livingTarget.addEffect(new MobEffectInstance(ModEffects.FRAGILE.get(), 160, 0));
                livingTarget.addEffect(new MobEffectInstance(ModEffects.SILENCE.get(), com.YqIl1.yqils_arknights.item.custom.LapplandBladeItem.SILENCE_DURATION_TICKS, 0));
            }

            // The projectile doesn't carry a reference to the exact stack that fired it, so this counts
            // the hit against whichever Lappland Blade the owner is still holding, if any
            if (this.getOwner() instanceof LivingEntity owner) {
                ItemStack mainHand = owner.getMainHandItem();
                ItemStack offHand = owner.getOffhandItem();
                if (mainHand.getItem() instanceof LapplandBladeItem) {
                    LapplandBladeItem.registerHit(mainHand, owner);
                } else if (offHand.getItem() instanceof LapplandBladeItem) {
                    LapplandBladeItem.registerHit(offHand, owner);
                }
            }

            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}
