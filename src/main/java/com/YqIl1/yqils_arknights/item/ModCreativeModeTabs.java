package com.YqIl1.yqils_arknights.item;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// The mod's own creative tab, holding all yqils_arknights items
public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Yqils_arknights.MODID);

    public static final RegistryObject<CreativeModeTab> YQILS_ARKNIGHTS_TAB = CREATIVE_MODE_TABS.register("yqils_arknights_tab",
            () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.LAPPLAND_BLADE.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.yqils_arknights.yqils_arknights_tab"))
            .displayItems((parameters, output) -> {

                output.accept(ModItems.AMBIGUOUS_MORALITY_MUSIC_DISC.get());
                output.accept(ModItems.LAPPLAND_BLADE.get());
                output.accept(ModItems.WEAPON_FORGING_STATION_ITEM.get());
                output.accept(ModItems.GEAR_BASE.get());
                output.accept(ModItems.MLYNAR_NEWSPAPER.get());
                output.accept(ModItems.EYJAFJALLA_WAND.get());

            }).build());
}
