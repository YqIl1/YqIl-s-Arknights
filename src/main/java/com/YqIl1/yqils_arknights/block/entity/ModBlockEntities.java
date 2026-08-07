package com.YqIl1.yqils_arknights.block.entity;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Yqils_arknights.MODID);
    public static final RegistryObject<BlockEntityType<WeaponForgingStationBlockEntity>> WEAPON_FORGING_BE =
            BLOCK_ENTITIES.register("weapon_forging_be", () ->
                    BlockEntityType.Builder.of(WeaponForgingStationBlockEntity::new,
                            ModBlocks.WEAPON_FORGING_STATION.get()).build(null));
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
