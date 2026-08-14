package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.item.custom.IfritFlamethrowerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

public class IfritFuelTankRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final GeoObjectRenderer<IfritFuelTank> RENDERER = new GeoObjectRenderer<>(new IfritFuelTankModel());

    // GeoObjectRenderer has no built-in "attach to body" logic (unlike GeckoLib's dedicated
    // GeoArmorRenderer), so the model renders at the raw entity origin (feet) with no correction.
    // Tweak these and rebuild to nudge the tank into place -- Y lifts it up toward the back/shoulders,
    // Z pulls it forward toward the body (increase magnitude if it's still too low/too far back).
    private static final double OFFSET_Y = -2.0D;
    private static final double OFFSET_Z = -0.5D;
    private static final double OFFSET_X = -0.5D;

    public IfritFuelTankRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                        float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (!(mainHand.getItem() instanceof IfritFlamethrowerItem) && !(offHand.getItem() instanceof IfritFlamethrowerItem)) {
            return;
        }

        poseStack.pushPose();
        // Counters the ambient scale(-1,-1,1) LivingEntityRenderer applies before layers render --
        // vanilla ModelParts are baked to expect that flip, GeckoLib's geometry is not.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(OFFSET_X, OFFSET_Y, OFFSET_Z);
        RENDERER.render(poseStack, IfritFuelTank.INSTANCE, bufferSource, null, null, packedLight);
        poseStack.popPose();
    }
}
