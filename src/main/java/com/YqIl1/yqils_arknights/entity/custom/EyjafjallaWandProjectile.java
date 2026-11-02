package com.YqIl1.yqils_arknights.entity.custom;

import com.YqIl1.yqils_arknights.entity.ModEntities;
import com.YqIl1.yqils_arknights.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class EyjafjallaWandProjectile extends ThrowableItemProjectile {
    private int MAX_LIFETIME = 60;
    private float damage = 6.0F;
    // The weapon that fired this, for the death message (WeaponMagicDamageSource). Not saved: it only lives 3 s.
    private net.minecraft.world.item.ItemStack weapon = net.minecraft.world.item.ItemStack.EMPTY;

    public void setWeapon(net.minecraft.world.item.ItemStack weapon) {
        this.weapon = weapon.copy();
    }

    public EyjafjallaWandProjectile(EntityType<? extends ThrowableItemProjectile> p_37442_, Level p_37443_) {
        super(p_37442_, p_37443_);
    }
    public EyjafjallaWandProjectile(Level level, LivingEntity shoote){
        super(ModEntities.EYJAFJALLA_WAND_PROJECTILE.get(), shoote, level);
    }

    // --- Volcano homing (EyjafjallaWandItem fires these during the skill) ---
    // Steers toward the target it was given at launch, turning at most MAX_TURN_DEGREES per tick at constant
    // speed. If that target dies (or there was none) it stops homing for good and flies on with gravity.
    private static final double MAX_TURN_DEGREES = 10.0D;
    @org.jetbrains.annotations.Nullable
    private LivingEntity homingTarget;

    public void setHomingTarget(@org.jetbrains.annotations.Nullable LivingEntity target) {this.homingTarget = target;}

    // Sets targets on fire: 3 s for normal shots, 5 s for Volcano lava shots.
    private static final int IGNITE_SECONDS = 3;
    private static final int VOLCANO_IGNITE_SECONDS = 5;
    private boolean volcano;

    public void setVolcano(boolean volcano) {this.volcano = volcano;}

    public void setDamage(float damage) {this.damage = damage;}
    public void setMaxLifetime(int maxLifetime) {this.MAX_LIFETIME = maxLifetime;}
    @Override
    protected Item getDefaultItem() {
        return ModItems.EYJAFJALLA_WAND_PROJECTILE.get();
    }

    @Override
    protected float getGravity() {
        return homingTarget != null ? 0.0F : 0.01f;
    }

    @Override
    public void tick() {
        if (homingTarget != null && !this.level().isClientSide) {
            steer();
        }
        super.tick();
        level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        if(!this.level().isClientSide && this.tickCount > MAX_LIFETIME){
            this.discard();
        }
    }

    private void steer() {
        if (!homingTarget.isAlive() || homingTarget.isRemoved() || homingTarget.level() != this.level()) {
            homingTarget = null; // target gone: keep the current heading, gravity takes over (getGravity)
            return;
        }
        net.minecraft.world.phys.Vec3 velocity = this.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 1.0E-4) return;
        net.minecraft.world.phys.Vec3 dir = velocity.scale(1.0D / speed);
        net.minecraft.world.phys.Vec3 aim = homingTarget.getBoundingBox().getCenter().subtract(this.position()).normalize();
        double angle = Math.acos(net.minecraft.util.Mth.clamp(dir.dot(aim), -1.0D, 1.0D));
        double maxTurn = Math.toRadians(MAX_TURN_DEGREES);
        net.minecraft.world.phys.Vec3 newDir = angle <= maxTurn ? aim
                : dir.scale(1.0D - maxTurn / angle).add(aim.scale(maxTurn / angle)).normalize();
        this.setDeltaMovement(newDir.scale(speed));
        this.hasImpulse = true; // sync the new heading to clients
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        DamageSource damageSource = com.YqIl1.yqils_arknights.damage.WeaponMagicDamageSource.magic(this, this.getOwner(), this.weapon);
        if (target.hurt(damageSource, this.damage)) {
            target.setSecondsOnFire(volcano ? VOLCANO_IGNITE_SECONDS : IGNITE_SECONDS);
        }


        this.discard();
    }
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}
