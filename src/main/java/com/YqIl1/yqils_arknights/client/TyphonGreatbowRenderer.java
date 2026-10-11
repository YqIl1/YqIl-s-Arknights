package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.custom.TyphonGreatbowItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

// DefaultedItemGeoModel resolves, for "typhon_greatbow":
//   geo/item/typhon_greatbow.geo.json, textures/item/typhon_greatbow.png, animations/item/typhon_greatbow.animation.json
public class TyphonGreatbowRenderer extends GeoItemRenderer<TyphonGreatbowItem> {
    public TyphonGreatbowRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(Yqils_arknights.MODID, "typhon_greatbow")));
    }
}
