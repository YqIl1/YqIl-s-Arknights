package com.YqIl1.yqils_arknights.entity.custom;

import com.YqIl1.yqils_arknights.entity.ModEntities;
import com.YqIl1.yqils_arknights.item.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

// Purely cosmetic -- Sword Rain's damage/stun is already applied directly in TexasBladeItem, this
// just drops a visual sword onto each hit target. Doesn't deal its own damage, and (per request)
// doesn't rotate to face its direction of travel like normal thrown projectiles do -- updateRotation
// is overridden to a no-op so it keeps a fixed orientation for its whole fall.
public class TexasBladeProjectile extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME_TICKS = 20; // 1 second safety despawn

    public TexasBladeProjectile(EntityType<? extends TexasBladeProjectile> type, Level level) {
        super(type, level);
    }

    public TexasBladeProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.TEXAS_BLADE_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.TEXAS_BLADE_PROJECTILE.get();
    }

    @Override
    protected float getGravity() {
        return 0.0F; // falls at whatever constant speed it's launched with, no acceleration
    }

    // Skips ThrowableProjectile#tick()'s normal per-tick "face direction of travel" update.
    @Override
    public void updateRotation() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount >= MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Deliberately not calling super.onHitEntity(...) -- no damage here, Sword Rain already dealt
        // it directly. This is just the falling-sword visual reaching its target.
        if (!this.level().isClientSide) {
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
