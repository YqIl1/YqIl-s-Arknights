package com.YqIl1.yqils_arknights.entity.custom;

import com.YqIl1.yqils_arknights.entity.ModEntities;
import com.YqIl1.yqils_arknights.item.custom.TyphonGreatbowItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Purely visual marker for Typhon's Greatbow lock-on: sits at the locked enemy's feet and follows it every
// tick. If the target dies or unloads it falls from where the target was last seen and rests on the ground
// (the bow reads its position, so the remaining skill arrows rain there). It's a server entity so every nearby player sees it; it's never saved to disk.
//
// Lifetime: the bow discards it when the lock ends (last shot / cancel). As a safety net it also removes
// itself if its owner is gone or no longer carries a bow whose lock points at this circle.
public class TyphonLockCircleEntity extends Entity {
    private static final int OWNER_CHECK_INTERVAL = 10; // ticks
    // Falling once the target is gone (same feel as vanilla item/arrow gravity).
    private static final double GRAVITY = 0.04D;
    private static final double AIR_DRAG = 0.98D;
    private static final double MAX_FALL_SPEED = 3.0D;

    private double fallSpeed;

    @Nullable private UUID targetId;
    @Nullable private UUID ownerId;

    public TyphonLockCircleEntity(EntityType<? extends TyphonLockCircleEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public TyphonLockCircleEntity(Level level, LivingEntity target, Player owner) {
        this(ModEntities.TYPHON_LOCK_CIRCLE.get(), level);
        this.targetId = target.getUUID();
        this.ownerId = owner.getUUID();
        this.setPos(target.getX(), target.getY(), target.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.targetId != null
                && serverLevel.getEntity(this.targetId) instanceof LivingEntity target && target.isAlive()) {
            this.setPos(target.getX(), target.getY(), target.getZ());
            this.fallSpeed = 0.0D;
        } else {
            fall(serverLevel);
        }

        if (this.tickCount % OWNER_CHECK_INTERVAL == 0 && !ownerStillLocked(serverLevel)) {
            this.discard();
        }
    }

    // Target dead/unloaded: drop straight down until the ground (or a liquid surface), and fall again if the
    // block underneath is removed. Uses a downward ray from the centre point instead of normal entity collision,
    // because the 9-block-wide hitbox would snag on any wall or tree beside the landing spot.
    private void fall(ServerLevel level) {
        if (this.getY() < level.getMinBuildHeight() - 16) {
            return; // fell into the void -- the owner check will discard it once the lock ends
        }
        this.fallSpeed = Math.min(this.fallSpeed * AIR_DRAG + GRAVITY, MAX_FALL_SPEED);
        Vec3 from = new Vec3(this.getX(), this.getY() + 0.001D, this.getZ());
        Vec3 to = new Vec3(this.getX(), this.getY() - this.fallSpeed, this.getZ());
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        if (hit.getType() == HitResult.Type.MISS) {
            this.setPos(to.x, to.y, to.z);
        } else {
            this.setPos(this.getX(), hit.getLocation().y, this.getZ());
            this.fallSpeed = 0.0D;
        }
    }

    private boolean ownerStillLocked(ServerLevel level) {
        if (this.ownerId == null) return false;
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(this.ownerId);
        if (owner == null) return false;
        for (ItemStack stack : owner.getInventory().items) {
            if (TyphonGreatbowItem.isLockCircle(stack, this.getUUID())) return true;
        }
        for (ItemStack stack : owner.getInventory().offhand) {
            if (TyphonGreatbowItem.isLockCircle(stack, this.getUUID())) return true;
        }
        return false;
    }

    // Never written to the world save -- a reload simply drops the visual.
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
