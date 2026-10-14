package com.YqIl1.yqils_arknights;

import com.YqIl1.yqils_arknights.block.ModBlocks;
import com.YqIl1.yqils_arknights.block.entity.ModBlockEntities;
import com.YqIl1.yqils_arknights.block.entity.renderer.WeaponForgingStationBlockEntityRenderer;
import com.YqIl1.yqils_arknights.client.CrosshairCounterOverlay;
import com.YqIl1.yqils_arknights.client.IfritFuelTankRenderLayer;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.entity.ModEntities;
import com.YqIl1.yqils_arknights.item.ModCreativeModeTabs;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.potion.ModPotions;
import com.YqIl1.yqils_arknights.recipe.ModRecipes;
import com.YqIl1.yqils_arknights.screen.ModMenuTypes;
import com.YqIl1.yqils_arknights.screen.WeaponForgingStationScreen;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
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
        com.YqIl1.yqils_arknights.particle.ModParticles.PARTICLE_TYPES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModPotions.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        event.enqueueWork(com.YqIl1.yqils_arknights.network.ModNetwork::register);
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));

        // Awkward Potion + Glass Block -> Potion of Fragile. Splash/Lingering variants come for free
        // via vanilla's generic gunpowder/dragon's-breath container conversion, no extra registration.
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD)),
                Ingredient.of(Items.GLASS),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.FRAGILE.get())));

        // Potion of Fragile + Glowstone Dust -> Potion of Fragile II (amplifier 1, half duration),
        // same shape as vanilla's own base-potion amplify mixes.
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.FRAGILE.get())),
                Ingredient.of(Items.GLOWSTONE_DUST),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.FRAGILE_II.get())));

        // Potion of Fragile + Redstone Dust -> Potion of Fragile (Extended duration), same shape as
        // vanilla's own base-potion extend mixes.
        event.enqueueWork(() -> BrewingRecipeRegistry.addRecipe(
                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.FRAGILE.get())),
                Ingredient.of(Items.REDSTONE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.FRAGILE_LONG.get())));
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

            // Drives the "using" predicate in ifrit_flamethrower_using.json's model overrides, same mechanism
            // vanilla uses for the bow's "pulling" model swap
            event.enqueueWork(() -> ItemProperties.register(ModItems.IFRIT_FLAMETHROWER.get(), new ResourceLocation("using"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F));

            // Vanilla only wires the "blocking" predicate up for the actual Items.SHIELD instance
            // (ItemProperties.java), not the ShieldItem class generically -- drives the override in
            // nearl_shield.json pointing at nearl_shield_blocking.json while raised.
            event.enqueueWork(() -> ItemProperties.register(ModItems.NEARL_SHIELD.get(), new ResourceLocation("blocking"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F));
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.LAPPLAND_BLADE_PROJECTILE.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(ModEntities.EYJAFJALLA_WAND_PROJECTILE.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(ModEntities.TEXAS_BLADE_PROJECTILE.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(ModEntities.TYPHON_LOCK_CIRCLE.get(), com.YqIl1.yqils_arknights.client.TyphonLockCircleRenderer::new);
            event.registerEntityRenderer(ModEntities.THROWN_CEOBE_SPEAR.get(), com.YqIl1.yqils_arknights.client.ThrownCeobeSpearRenderer::new);
            MenuScreens.register(ModMenuTypes.WEAPON_FORGING_MENU.get(), WeaponForgingStationScreen::new);

            event.registerBlockEntityRenderer(ModBlockEntities.WEAPON_FORGING_BE.get(), WeaponForgingStationBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerParticleProviders(net.minecraftforge.client.event.RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(com.YqIl1.yqils_arknights.particle.ModParticles.TYPHON_STAR.get(),
                    com.YqIl1.yqils_arknights.client.TyphonStarParticle.Provider::new);
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
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            for (String skinName : event.getSkins()) {
                PlayerRenderer renderer = event.getSkin(skinName);
                renderer.addLayer(new IfritFuelTankRenderLayer(renderer));
            }
        }

        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "crosshair_counter", new CrosshairCounterOverlay());
        }
    }
}
