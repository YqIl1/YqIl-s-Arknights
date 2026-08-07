package com.YqIl1.yqils_arknights.event;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Defaults to the Forge event bus (game events), unlike the mod event bus used for registration/setup
@Mod.EventBusSubscriber(modid = Yqils_arknights.MODID)
public class ModEventHandlers {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.FRAGILE.get())) {
            MobEffectInstance instance = entity.getEffect(ModEffects.FRAGILE.get());
            int level = instance.getAmplifier() + 1;
            float multiplier = 1.0F + (0.10F * level);
            event.setAmount(event.getAmount() * multiplier);
        }
    }





}
