package com.YqIl1.yqils_arknights.entity;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.entity.custom.EyjafjallaWandProjectile;
import com.YqIl1.yqils_arknights.entity.custom.LapplandBladeProjectile;
import com.YqIl1.yqils_arknights.entity.custom.TexasBladeProjectile;
import com.YqIl1.yqils_arknights.entity.custom.TyphonLockCircleEntity;
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

    public static final RegistryObject<EntityType<EyjafjallaWandProjectile>> EYJAFJALLA_WAND_PROJECTILE = ENTITY_TYPES.register("eyjafjalla_wand_projectile",
            () -> EntityType.Builder.<EyjafjallaWandProjectile>of(EyjafjallaWandProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("eyjafjalla_wand_projectile"));

    public static final RegistryObject<EntityType<TexasBladeProjectile>> TEXAS_BLADE_PROJECTILE = ENTITY_TYPES.register("texas_blade_projectile",
            () -> EntityType.Builder.<TexasBladeProjectile>of(TexasBladeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("texas_blade_projectile"));

    // Visual-only lock-on marker for Typhon's Greatbow. 9 wide (same as the drawn circle) so the circle isn't frustum-culled while the
    // target's feet are just off-screen; updateInterval 1 so it keeps up with fast-moving targets.
    public static final RegistryObject<EntityType<TyphonLockCircleEntity>> TYPHON_LOCK_CIRCLE = ENTITY_TYPES.register("typhon_lock_circle",
            () -> EntityType.Builder.<TyphonLockCircleEntity>of(TyphonLockCircleEntity::new, MobCategory.MISC)
                    .sized(9.0F, 0.1F)
                    .noSave()
                    .fireImmune()
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("typhon_lock_circle"));
}
