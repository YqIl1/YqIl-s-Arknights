package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.custom.CeobeSpearItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

// DefaultedItemGeoModel resolves, for "ceobe_spear":
//   geo/item/ceobe_spear.geo.json, textures/item/ceobe_spear.png, animations/item/ceobe_spear.animation.json
public class CeobeSpearRenderer extends GeoItemRenderer<CeobeSpearItem> {
    public CeobeSpearRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(Yqils_arknights.MODID, "ceobe_spear")));
    }
}
