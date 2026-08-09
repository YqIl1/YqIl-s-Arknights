package com.YqIl1.yqils_arknights.effect;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.effect.custom.EyjafjallaWandPowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.FragileEffect;
import com.YqIl1.yqils_arknights.effect.custom.LapplandBladePowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.MlynarPowerEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Central place to register every MobEffect under the "yqils_arknights" namespace
public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Yqils_arknights.MODID);

    // Damage taken is increased by 10% per level (handled in ModEventHandlers)
    public static final RegistryObject<MobEffect> FRAGILE = MOB_EFFECTS.register("fragile",
            () -> new FragileEffect(MobEffectCategory.HARMFUL, 0x898989));

    // +11 attack damage, +2 block reach -- both handled natively by the attribute modifiers baked into the effect
    public static final RegistryObject<MobEffect> MLYNAR_POWER = MOB_EFFECTS.register("mlynar_power",
            () -> new MlynarPowerEffect(MobEffectCategory.BENEFICIAL, 0xE3CF89));

    // +120% attack damage, granted when Lappland's Blade's hit counter reaches its threshold
    public static final RegistryObject<MobEffect> LAPPLAND_BLADE_POWER = MOB_EFFECTS.register("lappland_blade_power",
            () -> new LapplandBladePowerEffect(MobEffectCategory.BENEFICIAL, 0xE84C4C));

    // ATK +130%; Range increases; Attack Interval reduces significantly; Attack becomes randomly shooting lava that can hit at most 6 enemies within range
    public static final RegistryObject<MobEffect> EYJAFJALLA_WAND_POWER = MOB_EFFECTS.register("eyjafjalla_wand_power",
            () -> new EyjafjallaWandPowerEffect(MobEffectCategory.BENEFICIAL, 0xED96A9));


}
