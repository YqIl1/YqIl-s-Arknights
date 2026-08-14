package com.YqIl1.yqils_arknights.item;

import com.YqIl1.yqils_arknights.block.ModBlocks;
import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.custom.EyjafjallaWandItem;
import com.YqIl1.yqils_arknights.item.custom.GearBaseItem;
import com.YqIl1.yqils_arknights.item.custom.IfritFlamethrowerItem;
import com.YqIl1.yqils_arknights.item.custom.LapplandBladeItem;
import com.YqIl1.yqils_arknights.item.custom.MlynarNewspaperItem;
import com.YqIl1.yqils_arknights.sound.ModSound;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// Central place to register every Item (and BlockItem) under the "yqils_arknights" namespace
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Yqils_arknights.MODID);

    public static final RegistryObject<Item> LAPPLAND_BLADE = ITEMS.register("lappland_blade",
            LapplandBladeItem::new);

    public static final RegistryObject<Item> LAPPLAND_BLADE_PROJECTILE = ITEMS.register("lappland_blade_projectile",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> AMBIGUOUS_MORALITY_MUSIC_DISC = ITEMS.register("ambiguous_morality_music_disc",
            ()->new RecordItem(6, ModSound.AMBIGUOUS_MORALITY, new Item.Properties().stacksTo(1), 4720));


    public static final RegistryObject<Item> WEAPON_FORGING_STATION_ITEM = ITEMS.register("weapon_forging_station",
            () -> new BlockItem(ModBlocks.WEAPON_FORGING_STATION.get(), new Item.Properties()));

    public static final RegistryObject<Item> MLYNAR_NEWSPAPER =  ITEMS.register("mlynar_newspaper",
            () -> new MlynarNewspaperItem(new Item.Properties()));

    public static final RegistryObject<Item> GEAR_BASE = ITEMS.register("gear_base",
            () -> new GearBaseItem(new Item.Properties()));

    public static final RegistryObject<Item> EYJAFJALLA_WAND = ITEMS.register("eyjafjalla_wand",
            () -> new EyjafjallaWandItem(new Item.Properties()));

    public static final RegistryObject<Item> EYJAFJALLA_WAND_PROJECTILE = ITEMS.register("eyjafjalla_wand_projectile",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IFRIT_FLAMETHROWER = ITEMS.register("ifrit_flamethrower",
            () -> new IfritFlamethrowerItem(new Item.Properties()));
}
