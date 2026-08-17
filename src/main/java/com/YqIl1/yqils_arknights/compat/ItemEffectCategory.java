package com.YqIl1.yqils_arknights.compat;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ItemEffectCategory implements IRecipeCategory<ItemEffectDisplay> {
    public static final RecipeType<ItemEffectDisplay> RECIPE_TYPE =
            RecipeType.create(Yqils_arknights.MODID, "item_effect", ItemEffectDisplay.class);

    // Effect icon textures are 128x128 source art (see textures/mob_effect/), scaled down to this
    // on-screen size to match JEI's usual icon scale.
    private static final int ICON_DRAW_SIZE = 18;
    private static final int ICON_TEXTURE_SIZE = 128;

    private static final int SIDE_PADDING = 10; // extra space added to both the left and right edges
    private static final int WIDTH = 110 + SIDE_PADDING * 2;
    private static final int HEIGHT = 46;
    private static final int SLOT_X = 1 + SIDE_PADDING;
    private static final int ICON_X = 22 + SIDE_PADDING;
    private static final int TEXT_X = 44 + SIDE_PADDING;
    private static final int TEXT_MAX_WIDTH = WIDTH - TEXT_X - 4 - SIDE_PADDING;
    private static final int SLOT_Y = (HEIGHT - ICON_DRAW_SIZE) / 2;

    private final IDrawable icon;

    public ItemEffectCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(net.minecraft.world.item.Items.POTION);
    }

    @Override
    public RecipeType<ItemEffectDisplay> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.yqils_arknights.item_effect.title");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemEffectDisplay display, IFocusGroup focuses) {
        builder.addInputSlot(SLOT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(display.item());
    }

    @Override
    public void draw(ItemEffectDisplay display, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(display.effect());
        if (effectId != null) {
            ResourceLocation icon = new ResourceLocation(effectId.getNamespace(),
                    "textures/mob_effect/" + effectId.getPath() + ".png");

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(ICON_X, SLOT_Y - 1, 0);
            guiGraphics.pose().scale((float) ICON_DRAW_SIZE / ICON_TEXTURE_SIZE, (float) ICON_DRAW_SIZE / ICON_TEXTURE_SIZE, 1.0F);
            guiGraphics.blit(icon, 0, 0, 0, 0, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
            guiGraphics.pose().popPose();
        }

        String subtitleKey = display.relation() == ItemEffectDisplay.Relation.GAIN
                ? "jei.yqils_arknights.item_effect.gain_label"
                : "jei.yqils_arknights.item_effect.apply_label";

        int seconds = display.durationTicks() / 20;
        Component subtitle = Component.translatable(subtitleKey);
        Component label = display.effect().getDisplayName();
        Component durationLabel = Component.literal(seconds + "s");

        Font font = Minecraft.getInstance().font;
        int y = 2;
        guiGraphics.drawString(font, subtitle, TEXT_X, y, 0x808080, false);
        y += font.lineHeight + 1;

        // Effect names that are too wide for the box wrap onto additional lines instead of running
        // past the edge -- Font#split breaks the text at TEXT_MAX_WIDTH and returns one entry per line.
        List<FormattedCharSequence> nameLines = font.split(label, TEXT_MAX_WIDTH);
        for (FormattedCharSequence line : nameLines) {
            guiGraphics.drawString(font, line, TEXT_X, y, 0x404040, false);
            y += font.lineHeight;
        }

        guiGraphics.drawString(font, durationLabel, TEXT_X, y + 1, 0x808080, false);
    }

    // Hovering the effect icon shows its own .description text (the same wording used in its in-world
    // tooltip) -- lets players check exactly what the effect does without leaving the recipe lookup.
    @Override
    public List<Component> getTooltipStrings(ItemEffectDisplay display, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        boolean overIcon = mouseX >= ICON_X && mouseX < ICON_X + ICON_DRAW_SIZE
                && mouseY >= SLOT_Y - 1 && mouseY < SLOT_Y - 1 + ICON_DRAW_SIZE;
        if (!overIcon) {
            return List.of();
        }

        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(display.effect());
        if (effectId == null) {
            return List.of();
        }

        return List.of(Component.translatable("effect." + effectId.getNamespace() + "." + effectId.getPath() + ".description"));
    }
}
