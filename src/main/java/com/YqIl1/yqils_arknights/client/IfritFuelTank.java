package com.YqIl1.yqils_arknights.client;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

// Not tied to any ItemStack -- this is a single shared object that only exists to give the
// fuel tank's GeoModel something to render, since GeckoLib always renders a GeoAnimatable.
public class IfritFuelTank implements GeoAnimatable {
    public static final IfritFuelTank INSTANCE = new IfritFuelTank();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private IfritFuelTank() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations -- the model only ever renders its static bind pose
    }

    @Override
    public double getTick(Object object) {
        return 0;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
