package com.YqIl1.yqils_arknights.datagen;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {

    public ModItemTagGenerator(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagLookup<Block>> p_275322_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_275343_, p_275729_, p_275322_, Yqils_arknights.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(ItemTags.MUSIC_DISCS).add(ModItems.AMBIGUOUS_MORALITY_MUSIC_DISC.get());
        this.tag(ItemTags.CREEPER_DROP_MUSIC_DISCS).add(ModItems.AMBIGUOUS_MORALITY_MUSIC_DISC.get());

        //Sword
        this.tag(ItemTags.SWORDS).add(ModItems.LAPPLAND_BLADE.get());


        //Weapons
        this.tag(ModTags.Items.WEAPONS).add(ModItems.LAPPLAND_BLADE.get());
        this.tag(ModTags.Items.WEAPONS).add(ModItems.EYJAFJALLA_WAND.get());
        this.tag(ModTags.Items.WEAPONS).add(ModItems.IFRIT_FLAMETHROWER.get());
        this.tag(ModTags.Items.SUPPORTS).add(ModItems.NEARL_SHIELD.get());

        //Supports
        this.tag(ModTags.Items.SUPPORTS).add(ModItems.MLYNAR_NEWSPAPER.get());
        this.tag(ModTags.Items.SUPPORTS).add(ModItems.NCDEER_FAN.get());

        //No Melee
        this.tag(ModTags.Items.NO_MELEE).add(ModItems.EYJAFJALLA_WAND.get());
        this.tag(ModTags.Items.NO_MELEE).add(ModItems.IFRIT_FLAMETHROWER.get());
    }
}