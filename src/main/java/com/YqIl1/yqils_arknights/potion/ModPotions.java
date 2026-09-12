package com.YqIl1.yqils_arknights.potion;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, Yqils_arknights.MODID);

    public static final RegistryObject<Potion> FRAGILE = POTIONS.register("fragile",
            () -> new Potion(new MobEffectInstance(ModEffects.FRAGILE.get(), 1800, 0))); // 1:30

    public static final RegistryObject<Potion> FRAGILE_II = POTIONS.register("strong_fragile",
            () -> new Potion(new MobEffectInstance(ModEffects.FRAGILE.get(), 900, 1))); // 0:45, amplifier 1

    public static final RegistryObject<Potion> FRAGILE_LONG = POTIONS.register("long_fragile",
            () -> new Potion(new MobEffectInstance(ModEffects.FRAGILE.get(), 4800, 0))); // 4:00, extended duration


    public static void register(IEventBus bus) {
        POTIONS.register(bus);
    }
}
