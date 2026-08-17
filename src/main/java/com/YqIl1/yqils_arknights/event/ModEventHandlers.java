package com.YqIl1.yqils_arknights.event;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.effect.custom.MlynarPowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.NCDeerFanPowerEffect;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.item.custom.LapplandBladeItem;
import com.YqIl1.yqils_arknights.util.ModTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.*;
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

        if (entity.hasEffect(ModEffects.MLYNAR_POWER.get())
                && entity.getPersistentData().getBoolean(MlynarPowerEffect.THRESHOLD_MET_KEY)) {
            event.setAmount(event.getAmount() * (1.0F - MlynarPowerEffect.THRESHOLD_DAMAGE_REDUCTION));
        }

        if (entity.hasEffect(ModEffects.NC_DEER_FAN_POWER.get())) {
            event.setAmount(event.getAmount() * (1.0F - NCDeerFanPowerEffect.DAMAGE_REDUCTION));
        }
    }

    // Vanilla hardcodes melee damage to damageSources().playerAttack(...) inside Player.attack(),
    // with no item-level hook to change the damage type -- so while Lappland's Power is active,
    // cancel the original hit and re-apply the same amount through a magic damage source instead.
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.PLAYER_ATTACK)) {
            return;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof LapplandBladeItem)) {
            return;
        }
        if (!player.hasEffect(ModEffects.LAPPLAND_BLADE_POWER.get())) {
            return;
        }
        LivingEntity target = event.getEntity();
        float amount = event.getAmount();
        event.setCanceled(true);
        target.hurt(player.damageSources().indirectMagic(player, player), amount);
        player.getMainHandItem().hurtEnemy(target, player);
    }

    //Not allowing wand items to damage with melee
    @SubscribeEvent
    public static void onLivingAttack(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.PLAYER_ATTACK)) {
            return;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return;
        }
        if (!(player.getMainHandItem().is(ModTags.Items.NO_MELEE))) {
            return;
        }
        event.setCanceled(true);
    }

    // Each kill while Unbrilliant Glory is active decays its attack damage boost, capped at 20 stacks
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        if (!player.hasEffect(ModEffects.MLYNAR_POWER.get())) {
            return;
        }

        var data = player.getPersistentData();
        int kills = data.getInt(MlynarPowerEffect.KILL_COUNT_KEY);
        data.putInt(MlynarPowerEffect.KILL_COUNT_KEY, Math.min(kills + 1, 20));
    }

    // Reset the decay counter whenever Unbrilliant Glory is (re)applied, so re-casting it clears past decay
    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() == ModEffects.MLYNAR_POWER.get()) {
            event.getEntity().getPersistentData().putInt(MlynarPowerEffect.KILL_COUNT_KEY, 0);
        }
    }
}
