package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.item.custom.CrosshairCounterItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

// Single overlay covering every item that implements CrosshairCounterItem (cooldowns, charge banks,
// hit counters) -- replaces what used to be one near-identical overlay class per item.
public class CrosshairCounterOverlay implements IGuiOverlay {
    private static final int OFFSET_X = 12; // to the right of the crosshair
    private static final int OFFSET_Y = -4;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack stack;
        boolean offhand;
        if (mainHand.getItem() instanceof CrosshairCounterItem) {
            stack = mainHand;
            offhand = false;
        } else if (offHand.getItem() instanceof CrosshairCounterItem) {
            stack = offHand;
            offhand = true;
        } else {
            return;
        }

        CrosshairCounterItem display = (CrosshairCounterItem) stack.getItem();
        String mainText = display.getCrosshairCounterText(stack, player.level());
        String secondaryText = display.getCrosshairSecondaryText(stack, player.level());

        Font font = Minecraft.getInstance().font;
        int mainY = screenHeight / 2 + OFFSET_Y;
        int mainX = offhand
                ? screenWidth / 2 - OFFSET_X - font.width(mainText)
                : screenWidth / 2 + OFFSET_X;
        guiGraphics.drawString(font, mainText, mainX, mainY, 0xFFFFFF);

        if (secondaryText != null) {
            int secondaryY = mainY - (font.lineHeight + 1);
            int secondaryX = offhand
                    ? screenWidth / 2 - OFFSET_X - font.width(secondaryText)
                    : screenWidth / 2 + OFFSET_X;
            guiGraphics.drawString(font, secondaryText, secondaryX, secondaryY, display.getCrosshairSecondaryColor());
        }
    }
}
