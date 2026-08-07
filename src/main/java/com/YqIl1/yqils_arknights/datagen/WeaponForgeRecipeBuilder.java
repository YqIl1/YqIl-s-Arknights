package com.YqIl1.yqils_arknights.datagen;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.recipe.WeaponForgeRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

// Mirrors WeaponForgeRecipe.Serializer's expected JSON shape exactly, so generated recipes can never drift out of sync with it
public class WeaponForgeRecipeBuilder {
    private final Item result;
    private final int count;
    private final Ingredient ingredient;

    public WeaponForgeRecipeBuilder(ItemLike result, int count) {
        this.result = result.asItem();
        this.count = count;
        this.ingredient = Ingredient.of(ModItems.GEAR_BASE.get());
    }

    public static WeaponForgeRecipeBuilder weaponForging(ItemLike result) {
        return new WeaponForgeRecipeBuilder(result, 1);
    }

    public void save(Consumer<FinishedRecipe> writer, String recipeId) {
        writer.accept(new Result(new ResourceLocation(Yqils_arknights.MODID, recipeId), result, count, ingredient));
    }

    private static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final Ingredient ingredient;

        public Result(ResourceLocation id, Item result, int count, Ingredient ingredient) {
            this.id = id;
            this.result = result;
            this.count = count;
            this.ingredient = ingredient;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            JsonArray ingredients = new JsonArray();
            ingredients.add(ingredient.toJson());
            json.add("ingredients", ingredients);

            JsonObject output = new JsonObject();
            output.addProperty("item", ForgeRegistries.ITEMS.getKey(result).toString());
            if (count > 1) {
                output.addProperty("count", count);
            }
            json.add("output", output);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return WeaponForgeRecipe.Serializer.INSTANCE;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
