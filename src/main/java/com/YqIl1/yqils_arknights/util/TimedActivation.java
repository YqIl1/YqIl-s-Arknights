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

    // ---- Effect-aware variants: the cooldown is paused while `holder` has `effect` --------------------
    // The cooldown starts when the skill's effect actually ends: at the time recorded by markEffectEnded
    // (written once, from ModEventHandlers#onSkillEffectEnded, when the effect expires or is removed early --
    // milk, Silence, death...), else at the nominal end (activation + effectDurationTicks). While the holder
    // still has the effect (e.g. it was extended), nothing counts and the skill isn't ready.
    // holder / effect may be null: then this behaves like the plain versions above.

    private static String endKey(String key) {
        return key + "_effect_end";
    }

    // Call once when the skill's effect ends on its holder.
    public static void markEffectEnded(ItemStack stack, Level level, String key) {
        stack.getOrCreateTag().putLong(endKey(key), level.getGameTime());
    }

    // True if the stack's skill was activated and its effect hasn't been recorded as ended yet.
    public static boolean awaitingEffectEnd(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key) && tag.getLong(endKey(key)) < tag.getLong(key);
    }

    private static long cooldownStart(CompoundTag tag, String key, int effectDurationTicks) {
        long lastUsed = tag.getLong(key);
        long ended = tag.getLong(endKey(key));
        return ended >= lastUsed && tag.contains(endKey(key)) ? ended : lastUsed + effectDurationTicks;
    }

    public static boolean isReady(ItemStack stack, Level level, String key, int effectDurationTicks, int cooldownTicks,
                                  @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity holder,
                                  @org.jetbrains.annotations.Nullable net.minecraft.world.effect.MobEffect effect) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key)) return true;
        if (holder != null && effect != null && holder.hasEffect(effect)) return false;
        return level.getGameTime() >= cooldownStart(tag, key, effectDurationTicks) + cooldownTicks;
    }

    public static int getElapsedSeconds(ItemStack stack, Level level, String key, int effectDurationTicks, int cooldownSeconds,
                                        @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity holder,
                                        @org.jetbrains.annotations.Nullable net.minecraft.world.effect.MobEffect effect) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key)) return cooldownSeconds;
        if (holder != null && effect != null && holder.hasEffect(effect)) return 0;
        long elapsedTicks = Math.max(0, level.getGameTime() - cooldownStart(tag, key, effectDurationTicks));
        return (int) Math.min(elapsedTicks / 20, cooldownSeconds);
    }
}
