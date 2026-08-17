package com.YqIl1.yqils_arknights.sound;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSound {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Yqils_arknights.MODID);
    public static final RegistryObject<SoundEvent> AMBIGUOUS_MORALITY = registerSoundEvents("ambiguous_morality");
    public static final RegistryObject<SoundEvent> MLYNAR_POWER = registerSoundEvents("mlynar_power");
    public static final RegistryObject<SoundEvent> LAPPLAND_BLADE_RANGED = registerSoundEvents("lappland_blade_ranged");
    public static final RegistryObject<SoundEvent> LAPPLAND_BLADE_MELEE = registerSoundEvents("lappland_blade_melee");
    public static final RegistryObject<SoundEvent> LAPPLAND_BLADE_POWER = registerSoundEvents("lappland_blade_power");
    public static final RegistryObject<SoundEvent> LAPPLAND_BLADE_POWER_MELEE = registerSoundEvents("lappland_blade_power_melee");
    public static final RegistryObject<SoundEvent> LAPPLAND_BLADE_POWER_RANGED = registerSoundEvents("lappland_blade_power_ranged");
    public static final RegistryObject<SoundEvent> EYJAFJALLA_WAND_RANGED = registerSoundEvents("eyjafjalla_wand_ranged");
    public static final RegistryObject<SoundEvent> EYJAFJALLA_WAND_POWER = registerSoundEvents("eyjafjalla_wand_power");
    public static final RegistryObject<SoundEvent> IFRIT_FLAMETHROWER_RANGED = registerSoundEvents("ifrit_flamethrower_ranged");
    public static final RegistryObject<SoundEvent> IFRIT_FLAMETHROWER_POWER = registerSoundEvents("ifrit_flamethrower_power");
    public static final RegistryObject<SoundEvent> IFRIT_FLAMETHROWER_POWER_RANGED = registerSoundEvents("ifrit_flamethrower_power_ranged");
    public static final RegistryObject<SoundEvent> NC_DEER_FAN_POWER = registerSoundEvents("ncdeer_fan_power");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Yqils_arknights.MODID, name)));
    }

    public static void register(IEventBus eventBus) {SOUND_EVENTS.register(eventBus);}
}