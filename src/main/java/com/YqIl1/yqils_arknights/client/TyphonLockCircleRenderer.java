package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.entity.custom.TyphonLockCircleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

// Draws textures/misc/typhon_lock_circle.png as a flat 9x9-block quad (ring at a 4-block radius) on the ground at
// the entity's position, slowly spinning, always full-bright so it reads in the dark.
public class TyphonLockCircleRenderer extends EntityRenderer<TyphonLockCircleEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Yqils_arknights.MODID, "textures/misc/typhon_lock_circle.png");
    // Texture is 9x9 blocks: ring at a 4-block radius (= TyphonGreatbowItem.SPLASH_RADIUS) plus room for the diamonds.
    private static final float HALF_SIZE = 4.5F;
    private static final float HEIGHT = 0.02F;       // just above the ground to avoid z-fighting
    private static final float SPIN_DEG_PER_TICK = 1.5F;

    public TyphonLockCircleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TyphonLockCircleEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, HEIGHT, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * SPIN_DEG_PER_TICK));

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        int light = LightTexture.FULL_BRIGHT;
        vertex(vc, pose, normal, -HALF_SIZE, -HALF_SIZE, 0.0F, 0.0F, light);
        vertex(vc, pose, normal, -HALF_SIZE,  HALF_SIZE, 0.0F, 1.0F, light);
        vertex(vc, pose, normal,  HALF_SIZE,  HALF_SIZE, 1.0F, 1.0F, light);
        vertex(vc, pose, normal,  HALF_SIZE, -HALF_SIZE, 1.0F, 0.0F, light);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer vc, Matrix4f pose, Matrix3f normal, float x, float z, float u, float v, int light) {
        vc.vertex(pose, x, 0.0F, z).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TyphonLockCircleEntity entity) {
        return TEXTURE;
    }
}
