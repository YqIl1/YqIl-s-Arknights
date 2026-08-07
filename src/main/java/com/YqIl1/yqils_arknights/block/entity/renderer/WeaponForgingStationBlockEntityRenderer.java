package com.YqIl1.yqils_arknights.block.entity.renderer;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.block.custom.WeaponForgingStationBlock;
import com.YqIl1.yqils_arknights.block.entity.WeaponForgingStationBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WeaponForgingStationBlockEntityRenderer implements BlockEntityRenderer<WeaponForgingStationBlockEntity> {
    // Matches the "printhand" group's origin in the original Blockbench model (see weapon_forging_station.json)
    private static final double PIVOT_X = 6 / 16.0;
    private static final double PIVOT_Y = 3 / 16.0;
    private static final double PIVOT_Z = 6 / 16.0;

    private static final float SWAY_SPEED = 0.3F;
    private static final float SWAY_DISTANCE = 3 / 16.0F; // how far it travels each direction, in blocks

    private static final ResourceLocation PRINTHAND_MODEL =
            new ResourceLocation(Yqils_arknights.MODID, "block/weapon_forging_station_printhand");

    // Where the floating item sits above the block, and how fast it spins -- adjust to taste
    private static final double ITEM_X = 0.5;
    private static final double ITEM_Y = 0.3;
    private static final double ITEM_Z = 0.5;
    private static final float ITEM_SCALE = 0.5F;
    private static final float ITEM_SPIN_SPEED = 2F;
    private static final float ITEM_TILT_DEGREES = 0;
    public WeaponForgingStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WeaponForgingStationBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BakedModel printhandModel = Minecraft.getInstance().getModelManager().getModel(PRINTHAND_MODEL);

        float xOffset = 0F;
        if (blockEntity.isProcessing()) {
            float time = blockEntity.getLevel().getGameTime() + partialTick;
            xOffset = Mth.sin(time * SWAY_SPEED) * SWAY_DISTANCE;
        }

        Direction facing = blockEntity.getBlockState().getValue(WeaponForgingStationBlock.FACING);
        float yRotation = getYRotationForFacing(facing);

        poseStack.pushPose();

        // Rotate the whole submodel to match the block's placed facing. The static base model gets this
        // automatically from the blockstate JSON; this submodel bypasses that pipeline entirely (rendered
        // directly via ModelManager/ModelRenderer), so it has to replicate the same rotation by hand or it
        // renders as if the block always faces north.
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        poseStack.translate(-0.5, 0, -0.5);

        // Sway left-right along the submodel's own local X axis, around the printhand's pivot. Because this
        // happens after the facing rotation above, "local X" already points the correct rotated direction.
        poseStack.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
        poseStack.translate(xOffset, 0, 0);
        poseStack.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), bufferSource.getBuffer(RenderType.cutout()),
                null, printhandModel, 1F, 1F, 1F, packedLight, packedOverlay);

        poseStack.popPose();

        renderFloatingItem(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    // Shows whichever item the station currently has -- the crafted result if there is one, otherwise the input
    private void renderFloatingItem(WeaponForgingStationBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                                     MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack renderStack = blockEntity.getRenderStack();
        if (renderStack.isEmpty()) {
            return;
        }

        float time = blockEntity.getLevel().getGameTime() + partialTick;

        poseStack.pushPose();
        poseStack.translate(ITEM_X, ITEM_Y, ITEM_Z);
        // Spin around vertical first (world-space, like a record turning), then tilt flat -- doing it in this
        // order keeps the spin axis vertical instead of the tilt bending it sideways
        poseStack.mulPose(Axis.YP.rotationDegrees(time * ITEM_SPIN_SPEED));
        poseStack.mulPose(Axis.XP.rotationDegrees(ITEM_TILT_DEGREES));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

        Minecraft.getInstance().getItemRenderer().renderStatic(renderStack, ItemDisplayContext.GROUND,
                packedLight, packedOverlay, poseStack, bufferSource, blockEntity.getLevel(), (int) blockEntity.getBlockPos().asLong());

        poseStack.popPose();
    }

    // Matches the y-rotation values used in the block's blockstate JSON (north=0, east=90, south=180, west=270)
    private static float getYRotationForFacing(Direction facing) {
        return ((facing.get2DDataValue() - Direction.NORTH.get2DDataValue() + 6) % 4) * 90F;
    }
}
