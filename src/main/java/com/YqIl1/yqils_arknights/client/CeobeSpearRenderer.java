package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.custom.CeobeSpearItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

// DefaultedItemGeoModel resolves, for "ceobe_spear":
//   geo/item/ceobe_spear.geo.json, textures/item/ceobe_spear.png, animations/item/ceobe_spear.animation.json
public class CeobeSpearRenderer extends GeoItemRenderer<CeobeSpearItem> {
    public CeobeSpearRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(Yqils_arknights.MODID, "ceobe_spear")));
    }

    // Third person: while charging a throw the arm swings up over the head (vanilla THROW_SPEAR pose),
    // which turns the spear around so the tip points backwards. Flip it 180 degrees around the grip
    // (the centre of the item's render space) so it points the same way as when held normally.
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        AbstractClientPlayer charger = findCharger(stack);
        boolean thirdPerson = context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        if (thirdPerson && charger != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            super.renderByItem(stack, context, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        } else {
            super.renderByItem(stack, context, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    // The player the client can see charging (using) this exact spear, or null.
    private static AbstractClientPlayer findCharger(ItemStack stack) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        for (AbstractClientPlayer player : level.players()) {
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof CeobeSpearItem
                    && (player.getUseItem() == stack || player.getItemInHand(player.getUsedItemHand()) == stack)) {
                return player;
            }
        }
        return null;
    }
}
