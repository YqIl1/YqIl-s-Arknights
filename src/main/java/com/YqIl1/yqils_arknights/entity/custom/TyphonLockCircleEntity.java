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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Purely visual marker for Typhon's Greatbow lock-on: sits at the locked enemy's feet and follows it every
// tick. If the target dies or unloads it stays where the target was last seen (same spot the remaining
// skill arrows rain on). It's a server entity so every nearby player sees it; it's never saved to disk.
//
// Lifetime: the bow discards it when the lock ends (last shot / cancel). As a safety net it also removes
// itself if its owner is gone or no longer carries a bow whose lock points at this circle.
public class TyphonLockCircleEntity extends Entity {
    private static final int OWNER_CHECK_INTERVAL = 10; // ticks

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
        }

        if (this.tickCount % OWNER_CHECK_INTERVAL == 0 && !ownerStillLocked(serverLevel)) {
            this.discard();
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
