package com.YqIl1.yqils_arknights.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Shared "activate a timed buff, cooldown starts once the buff itself ends" pattern used by several
// items (Mlynar's Newspaper, Eyjafjalla's Wand, NC Deer Fan, Nearl's Shield). Tracks a single
// "last activated" NBT timestamp on the stack and derives everything else from it analytically.
//
// Deliberately does NOT rewrite that timestamp on a per-tick basis while the buff is active (an
// earlier per-item pattern did this, nudging the timestamp forward every tick to keep the cooldown
// from starting early). That constant NBT churn trips LivingEntity's vanilla equipment-change
// detection (ItemStack.matches is NBT-sensitive) and causes a visible "reequip" glitch for as long
// as the buff lasts. Since "when did the buff end" is fully computable from the one activation
// timestamp plus the known buff duration, no repeated writes are ever needed.
public final class TimedActivation {
    private TimedActivation() {
    }

    // True once effectDurationTicks + cooldownTicks have passed since the last activation.
    public static boolean isReady(ItemStack stack, Level level, String key, int effectDurationTicks, int cooldownTicks) {
        return level.getGameTime() >= readyAtTick(stack, key, effectDurationTicks, cooldownTicks);
    }

    // Call once, at the moment the buff is actually (re)applied -- not every tick.
    public static void markActivated(ItemStack stack, Level level, String key) {
        stack.getOrCreateTag().putLong(key, level.getGameTime());
    }

    private static long readyAtTick(ItemStack stack, String key, int effectDurationTicks, int cooldownTicks) {
        CompoundTag tag = stack.getTag();
        long lastUsed = tag != null ? tag.getLong(key) : 0L;
        return lastUsed + effectDurationTicks + cooldownTicks;
    }

    // Seconds elapsed toward being ready again -- stays at 0 the whole time the buff is still active
    // (cooldown hasn't started yet), then counts up to cooldownSeconds once the buff ends.
    public static int getElapsedSeconds(ItemStack stack, Level level, String key, int effectDurationTicks, int cooldownSeconds) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return cooldownSeconds;
        long cooldownStartsAt = tag.getLong(key) + effectDurationTicks;
        long elapsedTicks = Math.max(0, level.getGameTime() - cooldownStartsAt);
        return (int) Math.min(elapsedTicks / 20, cooldownSeconds);
    }
}
