package com.YqIl1.yqils_arknights.screen;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.recipe.WeaponForgeRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class WeaponForgingStationScreen extends AbstractContainerScreen<WeaponForgingStationMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Yqils_arknights.MODID, "textures/gui/weapon_forging_station_gui.png");

    // Mirrors vanilla StonecutterScreen's geometry exactly, since this GUI texture follows the same layout
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_FULL_HEIGHT = 54;
    private static final int RECIPES_COLUMNS = 4;
    private static final int RECIPES_ROWS = 3;
    private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
    private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
    private static final int RECIPES_X = 52;
    private static final int RECIPES_Y = 14;

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;

    public WeaponForgingStationScreen(WeaponForgingStationMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelY -= 2;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        // Not part of the stonecutter layout -- adjust bounds to wherever fits your texture
        this.addRenderableWidget(Button.builder(Component.translatable("gui.yqils_arknights.weapon_forging_station.start"),
                        button -> {
                            Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, WeaponForgingStationMenu.START_BUTTON_ID);
                            button.setFocused(false);
                        })
                .bounds(x + 129, y + 58, 40, 20)
                .build());

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int i = this.leftPos;
        int j = this.topPos;
        guiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);

        renderProgressArrow(guiGraphics, i, j);

        List<WeaponForgeRecipe> recipes = menu.getRecipes();
        if (!recipes.isEmpty()) {
            int k = (int) (41.0F * this.scrollOffs);
            guiGraphics.blit(TEXTURE, i + 119, j + 15 + k, 176 + (isScrollBarActive(recipes) ? 0 : 12), 0, 12, 15);

            int l = this.leftPos + RECIPES_X;
            int i1 = this.topPos + RECIPES_Y;
            int j1 = this.startIndex + RECIPES_COLUMNS * RECIPES_ROWS;
            renderButtons(guiGraphics, mouseX, mouseY, l, i1, j1, recipes);
            renderRecipes(guiGraphics, l, i1, j1, recipes);
        }
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(TEXTURE, x + 54, y + 59, 18, 169, menu.getScaledProgress(), 8);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        List<WeaponForgeRecipe> recipes = menu.getRecipes();
        if (!recipes.isEmpty()) {
            int i = this.leftPos + RECIPES_X;
            int j = this.topPos + RECIPES_Y;
            int k = this.startIndex + RECIPES_COLUMNS * RECIPES_ROWS;

            for (int l = this.startIndex; l < k && l < recipes.size(); ++l) {
                int i1 = l - this.startIndex;
                int j1 = i + i1 % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH;
                int k1 = j + i1 / RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_HEIGHT + 2;
                if (mouseX >= j1 && mouseX < j1 + RECIPES_IMAGE_SIZE_WIDTH && mouseY >= k1 && mouseY < k1 + RECIPES_IMAGE_SIZE_HEIGHT) {
                    guiGraphics.renderTooltip(this.font, recipes.get(l).getResultItem(this.minecraft.level.registryAccess()), mouseX, mouseY);
                }
            }
        }
    }

    private void renderButtons(GuiGraphics guiGraphics, int mouseX, int mouseY, int gridX, int gridY, int endIndex, List<WeaponForgeRecipe> recipes) {
        for (int i = this.startIndex; i < endIndex && i < recipes.size(); ++i) {
            int j = i - this.startIndex;
            int k = gridX + j % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH;
            int l = j / RECIPES_COLUMNS;
            int i1 = gridY + l * RECIPES_IMAGE_SIZE_HEIGHT + 2;
            int j1 = this.imageHeight;
            if (i == menu.getSelectedRecipeIndex()) {
                j1 += RECIPES_IMAGE_SIZE_HEIGHT;
            } else if (mouseX >= k && mouseY >= i1 && mouseX < k + RECIPES_IMAGE_SIZE_WIDTH && mouseY < i1 + RECIPES_IMAGE_SIZE_HEIGHT) {
                j1 += RECIPES_IMAGE_SIZE_HEIGHT * 2;
            }

            guiGraphics.blit(TEXTURE, k, i1 - 1, 0, j1, RECIPES_IMAGE_SIZE_WIDTH, RECIPES_IMAGE_SIZE_HEIGHT);
        }
    }

    private void renderRecipes(GuiGraphics guiGraphics, int gridX, int gridY, int endIndex, List<WeaponForgeRecipe> recipes) {
        for (int i = this.startIndex; i < endIndex && i < recipes.size(); ++i) {
            int j = i - this.startIndex;
            int k = gridX + j % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH;
            int l = j / RECIPES_COLUMNS;
            int i1 = gridY + l * RECIPES_IMAGE_SIZE_HEIGHT + 2;
            guiGraphics.renderItem(recipes.get(i).getResultItem(this.minecraft.level.registryAccess()), k, i1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        List<WeaponForgeRecipe> recipes = menu.getRecipes();
        if (!recipes.isEmpty()) {
            int i = this.leftPos + RECIPES_X;
            int j = this.topPos + RECIPES_Y;
            int k = this.startIndex + RECIPES_COLUMNS * RECIPES_ROWS;

            for (int l = this.startIndex; l < k && l < recipes.size(); ++l) {
                int i1 = l - this.startIndex;
                double d0 = mouseX - (double) (i + i1 % RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_WIDTH);
                double d1 = mouseY - (double) (j + i1 / RECIPES_COLUMNS * RECIPES_IMAGE_SIZE_HEIGHT);
                if (d0 >= 0.0D && d1 >= 0.0D && d0 < RECIPES_IMAGE_SIZE_WIDTH && d1 < RECIPES_IMAGE_SIZE_HEIGHT && menu.clickMenuButton(this.minecraft.player, l)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick(menu.containerId, l);
                    return true;
                }
            }

            i = this.leftPos + 119;
            j = this.topPos + 9;
            if (mouseX >= i && mouseX < i + SCROLLER_WIDTH && mouseY >= j && mouseY < j + SCROLLER_FULL_HEIGHT) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        List<WeaponForgeRecipe> recipes = menu.getRecipes();
        if (this.scrolling && isScrollBarActive(recipes)) {
            int i = this.topPos + RECIPES_Y;
            int j = i + SCROLLER_FULL_HEIGHT;
            this.scrollOffs = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) getOffscreenRows(recipes)) + 0.5D) * RECIPES_COLUMNS;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<WeaponForgeRecipe> recipes = menu.getRecipes();
        if (isScrollBarActive(recipes)) {
            int rows = getOffscreenRows(recipes);
            float f = (float) delta / (float) rows;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) rows) + 0.5D) * RECIPES_COLUMNS;
        }
        return true;
    }

    private boolean isScrollBarActive(List<WeaponForgeRecipe> recipes) {
        return recipes.size() > RECIPES_COLUMNS * RECIPES_ROWS;
    }

    private int getOffscreenRows(List<WeaponForgeRecipe> recipes) {
        return (recipes.size() + RECIPES_COLUMNS - 1) / RECIPES_COLUMNS - RECIPES_ROWS;
    }
}
