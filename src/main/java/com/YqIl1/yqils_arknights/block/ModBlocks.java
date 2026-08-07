package com.YqIl1.yqils_arknights.block;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.block.custom.WeaponForgingStationBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Central place to register every Block under the "yqils_arknights" namespace
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Yqils_arknights.MODID);

    public static final RegistryObject<Block> WEAPON_FORGING_STATION = BLOCKS.register("weapon_forging_station",
            () -> new WeaponForgingStationBlock(BlockBehaviour.Properties.copy(Blocks.ANVIL).noOcclusion()));
}
