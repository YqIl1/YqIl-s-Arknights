package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.entity.custom.ThrownCeobeSpear;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// Draws the flying spear with the item's own GeckoLib model, pointed along its direction of travel
// (same rotation idea as vanilla ThrownTridentRenderer, but our model's tip points +Y instead of -Y).
public class ThrownCeobeSpearRenderer extends EntityRenderer<ThrownCeobeSpear> {
    private static final float SCALE = 0.75F;
    // Pull the model back along its length so the tip, not the grip, sits at the entity's position.
    private static final float TIP_OFFSET = 1.8F;

    private final ItemRenderer itemRenderer;
    private final ItemStack displayStack = new ItemStack(ModItems.CEOBE_SPEAR.get());

    public ThrownCeobeSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownCeobeSpear entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) - 90.0F));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(0.0F, -TIP_OFFSET, 0.0F);
        this.itemRenderer.renderStatic(this.displayStack, ItemDisplayContext.NONE, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownCeobeSpear entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
