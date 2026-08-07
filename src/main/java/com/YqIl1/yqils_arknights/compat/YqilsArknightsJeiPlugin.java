package com.YqIl1.yqils_arknights.compat;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.block.ModBlocks;
import com.YqIl1.yqils_arknights.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Collections;

@JeiPlugin
public class YqilsArknightsJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {

        return new ResourceLocation((Yqils_arknights.MODID + ":jei_plugin"));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        registration.addIngredientInfo(
                new ItemStack(ModItems.LAPPLAND_BLADE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("item.yqils_arknights.lappland_blade.jei_info")
        );

        registration.addIngredientInfo(
                new ItemStack(ModItems.MLYNAR_NEWSPAPER.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("item.yqils_arknights.mlynar_newspaper.jei_info")

        );

    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IModPlugin.super.registerCategories(registration);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        IModPlugin.super.registerGuiHandlers(registration);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        // The projectile item only exists to render the thrown blade visually and can't be obtained by players
        jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                Collections.singletonList(new ItemStack(ModItems.LAPPLAND_BLADE_PROJECTILE.get()))
        );
    }
}
