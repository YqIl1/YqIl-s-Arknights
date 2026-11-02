package com.YqIl1.yqils_arknights.item.custom;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Implemented by items that show a "current/max" counter near the crosshair while held -- cooldowns,
// charge banks, hit counters, etc. Rendered generically by CrosshairCounterOverlay, which checks for
// this interface instead of enumerating concrete item classes.
public interface CrosshairCounterItem {
    String getCrosshairCounterText(ItemStack stack, Level level);

    // What the overlay actually calls; override when the text depends on the holder (e.g. skill cooldowns
    // that pause while the holder has the skill's effect).
    default String getCrosshairCounterText(ItemStack stack, Level level, net.minecraft.world.entity.player.Player player) {
        return getCrosshairCounterText(stack, level);
    }

    // Optional second line rendered above the main counter (e.g. Ifrit's banked charge count).
    // Returning null skips the line entirely.
    default String getCrosshairSecondaryText(ItemStack stack, Level level) {
        return null;
    }

    default int getCrosshairSecondaryColor() {
        return 0xFFFF55;
    }
}
