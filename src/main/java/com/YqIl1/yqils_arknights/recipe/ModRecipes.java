package com.YqIl1.yqils_arknights.recipe;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Yqils_arknights.MODID);

    public static final RegistryObject<RecipeSerializer<WeaponForgeRecipe>> WEAPON_FORGING_SERIALIZER =
            SERIALIZERS.register("weapon_forging", ()->WeaponForgeRecipe.Serializer.INSTANCE);

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
