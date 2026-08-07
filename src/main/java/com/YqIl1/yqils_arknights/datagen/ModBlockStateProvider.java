package com.YqIl1.yqils_arknights.datagen;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.block.ModBlocks;
import com.YqIl1.yqils_arknights.block.custom.WeaponForgingStationBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Yqils_arknights.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(ModBlocks.WEAPON_FORGING_STATION.get(), new ModelFile.UncheckedModelFile(modLoc("block/weapon_forging_station_base")));




    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }
}
