package com.YqIl1.yqils_arknights.recipe;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Yqils_arknights.MODID);

    // RecipeType was previously never registered into any registry -- only the serializer was, which
    // left BuiltInRegistries.RECIPE_TYPE.getKey(WeaponForgeRecipe.Type.INSTANCE) with no entry, causing
    // ClientRecipeBook's "Unknown recipe category" NPE spam every time a client synced recipes.
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Yqils_arknights.MODID);

    public static final RegistryObject<RecipeSerializer<WeaponForgeRecipe>> WEAPON_FORGING_SERIALIZER =
            SERIALIZERS.register("weapon_forging", ()->WeaponForgeRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeType<?>> WEAPON_FORGING_TYPE =
            TYPES.register("weapon_forging", () -> WeaponForgeRecipe.Type.INSTANCE);

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }
}
