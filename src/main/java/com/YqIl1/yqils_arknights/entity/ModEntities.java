package com.YqIl1.yqils_arknights.entity;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.entity.custom.LapplandBladeProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Central place to register every EntityType under the "yqils_arknights" namespace
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Yqils_arknights.MODID);

    public static final RegistryObject<EntityType<LapplandBladeProjectile>> LAPPLAND_BLADE_PROJECTILE = ENTITY_TYPES.register("lappland_blade_projectile",
            () -> EntityType.Builder.<LapplandBladeProjectile>of(LapplandBladeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("lappland_blade_projectile"));
}
