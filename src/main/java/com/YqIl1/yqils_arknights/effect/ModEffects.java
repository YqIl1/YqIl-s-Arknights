package com.YqIl1.yqils_arknights.effect;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.effect.custom.EyjafjallaWandPowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.FragileEffect;
import com.YqIl1.yqils_arknights.effect.custom.ImmolationEffect;
import com.YqIl1.yqils_arknights.effect.custom.LapplandBladePowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.MlynarPowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.NCDeerFanPowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.NearlShieldPowerEffect;
import com.YqIl1.yqils_arknights.effect.custom.StunEffect;
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

    // Deals 1 fire damage per second; granted to targets hit by the flamethrower's charged skill activation
    public static final RegistryObject<MobEffect> IMMOLATION = MOB_EFFECTS.register("immolation",
            () -> new ImmolationEffect(MobEffectCategory.HARMFUL, 0xFF4500));

    // Granted by NC Deer Fan's hold-to-activate -- behavior TBD
    public static final RegistryObject<MobEffect> NC_DEER_FAN_POWER = MOB_EFFECTS.register("nc_deer_fan_power",
            () -> new NCDeerFanPowerEffect(MobEffectCategory.BENEFICIAL, 0xFAFAFA));

    // Heals 33% of the wielder's own ATTACK_DAMAGE every 2.5s -- granted by Nearl's Shield's
    // shift-right-click activation
    public static final RegistryObject<MobEffect> NEARL_SHIELD_POWER = MOB_EFFECTS.register("nearl_shield_power",
            () -> new NearlShieldPowerEffect(MobEffectCategory.BENEFICIAL, 0x8FD1E0));

    // Disables the target's AI for the effect's duration. G
    public static final RegistryObject<MobEffect> STUN = MOB_EFFECTS.register("stun",
            () -> new StunEffect(MobEffectCategory.HARMFUL, 0x888888));

    // "很重的枪" -- granted by Ceobe's Spear's right-click skill (60s). Behaviour TBD.
    public static final RegistryObject<MobEffect> CEOBE_POWER = MOB_EFFECTS.register("ceobe_power",
            () -> new com.YqIl1.yqils_arknights.effect.custom.CeobePowerEffect(MobEffectCategory.BENEFICIAL, 0xE0893A));

    // Applied by Lappland's Blade (The Young Fang). Behaviour lives in ModEventHandlers.
    public static final RegistryObject<MobEffect> SILENCE = MOB_EFFECTS.register("silence",
            () -> new com.YqIl1.yqils_arknights.effect.custom.SilenceEffect(MobEffectCategory.HARMFUL, 0xA0A5AD));
}
