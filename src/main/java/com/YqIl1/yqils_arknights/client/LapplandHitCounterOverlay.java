package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.item.custom.LapplandBladeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class LapplandHitCounterOverlay implements IGuiOverlay {
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
        if (mainHand.getItem() instanceof LapplandBladeItem) {
            stack = mainHand;
            offhand = false;
        } else if (offHand.getItem() instanceof LapplandBladeItem) {
            stack = offHand;
            offhand = true;
        } else {
            return;
        }

        String text = LapplandBladeItem.getHitCount(stack) + "/" + LapplandBladeItem.POWER_THRESHOLD;
        int y = screenHeight / 2 + OFFSET_Y;
        int x;
        if (offhand) {
            x = screenWidth / 2 - OFFSET_X - Minecraft.getInstance().font.width(text);
        } else {
            x = screenWidth / 2 + OFFSET_X;
        }

        guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFF);
    }
}
