package com.YqIl1.yqils_arknights.recipe;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WeaponForgeRecipe implements Recipe<SimpleContainer> {
    private final NonNullList<Ingredient> input;
    private final ItemStack output;
    private final ResourceLocation id;

    public WeaponForgeRecipe(NonNullList<Ingredient> input, ItemStack output, ResourceLocation id) {
        this.input = input;
        this.output = output;
        this.id = id;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return  input.get(0).test(pContainer.getItem(0));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return input;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    // Crafted through the Weapon Forging Station's own screen, not the vanilla crafting-table recipe
    // book -- isSpecial() = true tells ClientRecipeBook to skip recipe-book categorization for this
    // recipe entirely, instead of falling into its "Unknown recipe category" warning path.
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<WeaponForgeRecipe>{
        public static final Type INSTANCE = new Type();
        public static final String ID = "weapon_forging";
    }

    public static class Serializer implements RecipeSerializer<WeaponForgeRecipe>{
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Yqils_arknights.MODID, "weapon_forging");

        @Override
        public WeaponForgeRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<Ingredient> input = NonNullList.withSize(1, Ingredient.EMPTY);

            for(int i = 0; i < ingredients.size(); ++i) {
                input.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            return new WeaponForgeRecipe(input, output, pRecipeId);
        }

        @Override
        public @Nullable WeaponForgeRecipe fromNetwork(ResourceLocation pRecopeId, FriendlyByteBuf pBuffer) {
            int size = pBuffer.readInt();
            NonNullList<Ingredient> input = NonNullList.withSize(size, Ingredient.EMPTY);
            for(int i = 0; i < size; ++i) {
                input.set(i, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack output = pBuffer.readItem();
            return new WeaponForgeRecipe(input, output, pRecopeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, WeaponForgeRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.input.size());

            for(Ingredient ingredient : pRecipe.getIngredients()) {
                ingredient.toNetwork(pBuffer);

            }

            pBuffer.writeItemStack(pRecipe.getResultItem(null), false);
        }
    }
}
