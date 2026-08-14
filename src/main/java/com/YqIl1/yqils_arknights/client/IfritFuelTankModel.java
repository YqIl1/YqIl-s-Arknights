package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class IfritFuelTankModel extends GeoModel<IfritFuelTank> {
    @Override
    public ResourceLocation getModelResource(IfritFuelTank animatable) {
        return new ResourceLocation(Yqils_arknights.MODID, "geo/ifrit_fueltank.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IfritFuelTank animatable) {
        return new ResourceLocation(Yqils_arknights.MODID, "textures/item/ifrit_fueltank.png");
    }

    @Override
    public ResourceLocation getAnimationResource(IfritFuelTank animatable) {
        return new ResourceLocation(Yqils_arknights.MODID, "animations/ifrit_fueltank.animation.json");
    }
}
