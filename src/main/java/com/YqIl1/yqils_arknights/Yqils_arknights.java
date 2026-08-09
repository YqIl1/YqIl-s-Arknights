package com.YqIl1.yqils_arknights;

import com.YqIl1.yqils_arknights.block.ModBlocks;
import com.YqIl1.yqils_arknights.block.entity.ModBlockEntities;
import com.YqIl1.yqils_arknights.block.entity.renderer.WeaponForgingStationBlockEntityRenderer;
import com.YqIl1.yqils_arknights.client.EyjafjallaWandCooldownOverlay;
import com.YqIl1.yqils_arknights.client.LapplandHitCounterOverlay;
import com.YqIl1.yqils_arknights.client.MlynarNewspaperCooldownOverlay;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.entity.ModEntities;
import com.YqIl1.yqils_arknights.item.ModCreativeModeTabs;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.recipe.ModRecipes;
import com.YqIl1.yqils_arknights.screen.ModMenuTypes;
import com.YqIl1.yqils_arknights.screen.WeaponForgingStationScreen;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Yqils_arknights.MODID)
public class Yqils_arknights {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "yqils_arknights";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Yqils_arknights() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        ModBlocks.BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ModItems.ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so mob effects get registered
        ModEffects.MOB_EFFECTS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so entities get registered
        ModEntities.ENTITY_TYPES.register(modEventBus);

        ModSound.SOUND_EVENTS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModRecipes.SERIALIZERS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            // Without this, transparent pixels in the block's textures render as solid black instead of see-through
            event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(ModBlocks.WEAPON_FORGING_STATION.get(), RenderType.cutout()));
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.LAPPLAND_BLADE_PROJECTILE.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(ModEntities.EYJAFJALLA_WAND_PROJECTILE.get(), ThrownItemRenderer::new);
            MenuScreens.register(ModMenuTypes.WEAPON_FORGING_MENU.get(), WeaponForgingStationScreen::new);

            event.registerBlockEntityRenderer(ModBlockEntities.WEAPON_FORGING_BE.get(), WeaponForgingStationBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
            // Nothing (no blockstate, no item model) references this model directly -- it's only ever
            // baked and rendered manually by WeaponForgingStationBlockEntityRenderer, so it has to be
            // registered explicitly or ModelManager.getModel(...) just returns the missing/fallback model.
            // Plain ResourceLocation here, not ModelResourceLocation -- that's the item/blockstate-variant
            // lookup key, not what RegisterAdditional expects.
            event.register(new ResourceLocation(MODID, "block/weapon_forging_station_printhand"));
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "lappland_hit_counter", new LapplandHitCounterOverlay());
            event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "eyjafjalla_wand_cooldown", new EyjafjallaWandCooldownOverlay());
            event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "mlynar_newspaper_cooldown", new MlynarNewspaperCooldownOverlay());
        }
    }
}
