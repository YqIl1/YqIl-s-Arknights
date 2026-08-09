package com.YqIl1.yqils_arknights.datagen;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.recipe.WeaponForgeRecipe;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        WeaponForgeRecipeBuilder.weaponForging(ModItems.LAPPLAND_BLADE.get())
                .save(pWriter, "lappland_blade_from_weapon_forging");

        WeaponForgeRecipeBuilder.weaponForging(ModItems.MLYNAR_NEWSPAPER.get())
                .save(pWriter, "mlynar_newspaper_weapon_from_forging");

        WeaponForgeRecipeBuilder.weaponForging(ModItems.EYJAFJALLA_WAND.get())
                .save(pWriter, "eyjafjala_wand_from_forging");



        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WEAPON_FORGING_STATION_ITEM.get())
                .pattern("BNB")
                .pattern("BCB")
                .pattern("III")
                .define('B', Items.IRON_BARS)
                .define('N', Items.IRON_INGOT)
                .define('C', Items.COAL_BLOCK)
                .define('I', Items.IRON_BLOCK)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GEAR_BASE.get())
                .pattern("ICI")
                .pattern("CGC")
                .pattern("ICI")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COAL)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .save(pWriter);


    }
    protected static void oreSmelting(Consumer<FinishedRecipe> p_250654_, List<ItemLike> p_250172_, RecipeCategory p_250588_, ItemLike p_251868_, float p_250789_, int p_252144_, String p_251687_) {
        oreCooking(p_250654_, RecipeSerializer.SMELTING_RECIPE, p_250172_, p_250588_, p_251868_, p_250789_, p_252144_, p_251687_, "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> p_248775_, List<ItemLike> p_251504_, RecipeCategory p_248846_, ItemLike p_249735_, float p_248783_, int p_250303_, String p_251984_) {
        oreCooking(p_248775_, RecipeSerializer.BLASTING_RECIPE, p_251504_, p_248846_, p_249735_, p_248783_, p_250303_, p_251984_, "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> p_250791_, RecipeSerializer<? extends AbstractCookingRecipe> p_251817_, List<ItemLike> p_249619_, RecipeCategory p_251154_, ItemLike p_250066_, float p_251871_, int p_251316_, String p_251450_, String p_249236_) {
        for(ItemLike itemlike : p_249619_) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(new ItemLike[]{itemlike}), p_251154_, p_250066_, p_251871_, p_251316_, p_251817_)
                    .group(p_251450_).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(p_250791_, Yqils_arknights.MODID + ":" + getItemName(p_250066_) + p_249236_ + "_" + getItemName(itemlike));
        }

    }
    /*
    protected static void alterUpgrade(Consumer<FinishedRecipe> pWriter, ItemLike baseItem, ItemLike alterItem, String name) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.ALTER_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(baseItem),
                        Ingredient.of(ModItems.ALTER_ESSENCE),
                        RecipeCategory.COMBAT,
                        alterItem
                ).unlocks("has_alter_essence", has(ModItems.ALTER_ESSENCE))
                .save(writer, name + "_alter_upgrade");
    }

     */
}
