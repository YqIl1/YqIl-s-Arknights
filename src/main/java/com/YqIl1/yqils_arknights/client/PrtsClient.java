package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.custom.PRTSItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Right click with PRTS (either hand) opens the radial command menu instead of using the item. The entity and
// block under the crosshair are picked right now (up to PICK_RANGE blocks), before the menu frees the cursor.
@Mod.EventBusSubscriber(modid = Yqils_arknights.MODID, value = Dist.CLIENT)
public final class PrtsClient {
    private static final double PICK_RANGE = 64.0D;

    private PrtsClient() {
    }

    @SubscribeEvent
    public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!event.isUseItem() || player == null || mc.screen != null
                || !(player.getItemInHand(event.getHand()).getItem() instanceof PRTSItem)) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);

        float partial = mc.getFrameTime();
        Vec3 eye = player.getEyePosition(partial);
        Vec3 look = player.getViewVector(partial);
        Vec3 end = eye.add(look.scale(PICK_RANGE));

        HitResult blockHit = player.pick(PICK_RANGE, partial, false);
        double blockDistSqr = blockHit.getType() == HitResult.Type.MISS ? PICK_RANGE * PICK_RANGE
                : blockHit.getLocation().distanceToSqr(eye);
        AABB box = player.getBoundingBox().expandTowards(look.scale(PICK_RANGE)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e != player, blockDistSqr);

        Entity target = entityHit != null ? entityHit.getEntity() : null;
        BlockPos pos = blockHit instanceof BlockHitResult bhr && blockHit.getType() == HitResult.Type.BLOCK
                ? bhr.getBlockPos().relative(bhr.getDirection()) : null;

        mc.setScreen(new PrtsRadialScreen(target, pos));
    }
}
