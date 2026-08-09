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

    public EyjafjallaWandProjectile(EntityType<? extends ThrowableItemProjectile> p_37442_, Level p_37443_) {
        super(p_37442_, p_37443_);
    }
    public EyjafjallaWandProjectile(Level level, LivingEntity shoote){
        super(ModEntities.EYJAFJALLA_WAND_PROJECTILE.get(), shoote, level);
    }

    public void setDamage(float damage) {this.damage = damage;}
    public void setMaxLifetime(int maxLifetime) {this.MAX_LIFETIME = maxLifetime;}
    @Override
    protected Item getDefaultItem() {
        return ModItems.EYJAFJALLA_WAND_PROJECTILE.get();
    }

    @Override
    protected float getGravity() {
        return 0.01f;
    }

    @Override
    public void tick() {
        super.tick();
        level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        if(!this.level().isClientSide && this.tickCount > MAX_LIFETIME){
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        DamageSource damageSource = this.damageSources().thrown(this, this.getOwner());
        target.hurt(damageSource, this.damage);


        this.discard();
    }
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
}
