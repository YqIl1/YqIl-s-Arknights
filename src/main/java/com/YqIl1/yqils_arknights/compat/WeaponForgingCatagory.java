package com.YqIl1.yqils_arknights.compat;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.recipe.WeaponForgeRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class WeaponForgingCatagory implements IRecipeCategory<WeaponForgeRecipe> {
    public static final RecipeType<WeaponForgeRecipe> RECIPE_TYPE =
            RecipeType.create(Yqils_arknights.MODID, "weapon_forging", WeaponForgeRecipe.class);

    private final IDrawable icon;

    public WeaponForgingCatagory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.WEAPON_FORGING_STATION_ITEM.get()));
    }

    @Override
    public RecipeType<WeaponForgeRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.yqils_arknights.weapon_forging_station");
    }

    @Override
    public int getWidth() {
        return 82;
    }

    @Override
    public int getHeight() {
        return 34;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WeaponForgeRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 8)
                .setStandardSlotBackground()
                .addIngredients(recipe.getIngredients().get(0));

        builder.addOutputSlot(58, 8)
                .setOutputSlotBackground()
                .addItemStack(recipe.getResultItem(null));
    }
}
