package com.YqIl1.yqils_arknights.entity.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.entity.ModEntities;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.item.custom.CeobeSpearItem;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

// Projectile thrown by Ceobe's Spear during its skill (the spear itself stays in the thrower's hand).
// Modelled on vanilla ThrownTrident: hits one entity once and bounces off; vanishes as soon as it touches a
// block. Damage is the thrower's ATTACK_DAMAGE at the moment of the throw (so the skill's +210% applies);
// every hit silences the target for 5 s. Can never be picked up.
public class ThrownCeobeSpear extends AbstractArrow {
    private ItemStack spearItem = new ItemStack(ModItems.CEOBE_SPEAR.get());
    private boolean dealtDamage;
    private float throwDamage = 9.0F;

    // Gold trail (same gold as the spear's texture): a few dust particles spread along each tick's path.
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.92F, 0.71F, 0.35F), 1.0F);
    private static final int TRAIL_PARTICLES_PER_TICK = 4;

    public ThrownCeobeSpear(EntityType<? extends ThrownCeobeSpear> type, Level level) {
        super(type, level);
    }

    public ThrownCeobeSpear(Level level, LivingEntity owner, ItemStack stack, float damage) {
        super(ModEntities.THROWN_CEOBE_SPEAR.get(), owner, level);
        this.spearItem = stack.copy();
        this.throwDamage = damage;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.inGround) {
            for (int i = 0; i < TRAIL_PARTICLES_PER_TICK; i++) {
                double t = i / (double) TRAIL_PARTICLES_PER_TICK;
                this.level().addParticle(TRAIL,
                        this.xo + (this.getX() - this.xo) * t,
                        this.yo + (this.getY() - this.yo) * t,
                        this.zo + (this.getZ() - this.zo) * t,
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.spearItem.copy();
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return this.dealtDamage ? null : super.findHitEntity(start, end);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        float damage = this.throwDamage;
        if (target instanceof LivingEntity living) {
            damage += EnchantmentHelper.getDamageBonus(this.spearItem, living.getMobType());
        }
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().trident(this, owner == null ? this : owner);
        this.dealtDamage = true;
        if (target.hurt(source, damage)) {
            if (target.getType() == EntityType.ENDERMAN) {
                return;
            }
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(ModEffects.SILENCE.get(), CeobeSpearItem.SILENCE_DURATION_TICKS, 0));
                if (owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(living, livingOwner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, living);
                }
                this.doPostHurtEffects(living);
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        this.playSound(com.YqIl1.yqils_arknights.sound.ModSound.CEOBE_SPEAR_POWER_HIT.get(), 1.0F, 1.0F);
    }

    // Gone the moment it touches the ground (or any block).
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Spear", 10)) {
            this.spearItem = ItemStack.of(tag.getCompound("Spear"));
        }
        this.dealtDamage = tag.getBoolean("DealtDamage");
        if (tag.contains("ThrowDamage")) {
            this.throwDamage = tag.getFloat("ThrowDamage");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Spear", this.spearItem.save(new CompoundTag()));
        tag.putBoolean("DealtDamage", this.dealtDamage);
        tag.putFloat("ThrowDamage", this.throwDamage);
    }
}
