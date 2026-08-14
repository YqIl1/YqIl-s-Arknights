package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.item.custom.IfritFlamethrowerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class IfritFlamethrowerCooldownOverlay implements IGuiOverlay {
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
        if (mainHand.getItem() instanceof IfritFlamethrowerItem) {
            stack = mainHand;
            offhand = false;
        } else if (offHand.getItem() instanceof IfritFlamethrowerItem) {
            stack = offHand;
            offhand = true;
        } else {
            return;
        }

        String chargeText = String.valueOf(IfritFlamethrowerItem.getCharges(stack));
        int elapsed = IfritFlamethrowerItem.getRegenElapsedSeconds(stack, player.level());
        String cooldownText = elapsed + "/" + IfritFlamethrowerItem.CHARGE_COOLDOWN_SECONDS;

        int lineHeight = Minecraft.getInstance().font.lineHeight + 1;
        int cooldownY = screenHeight / 2 + OFFSET_Y;
        int chargeY = cooldownY - lineHeight;

        int cooldownX = offhand
                ? screenWidth / 2 - OFFSET_X - Minecraft.getInstance().font.width(cooldownText)
                : screenWidth / 2 + OFFSET_X;
        int chargeX = offhand
                ? screenWidth / 2 - OFFSET_X - Minecraft.getInstance().font.width(chargeText)
                : screenWidth / 2 + OFFSET_X;

        guiGraphics.drawString(Minecraft.getInstance().font, chargeText, chargeX, chargeY, 0xFFFF55);
        guiGraphics.drawString(Minecraft.getInstance().font, cooldownText, cooldownX, cooldownY, 0xFFFFFF);
    }
}
