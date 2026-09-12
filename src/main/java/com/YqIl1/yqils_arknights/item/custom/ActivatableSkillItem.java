package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.TimedActivation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

// Implemented by items whose skill is "grant an effect, then go on a cooldown that starts once the
// effect ends" (Mlynar's Newspaper, Eyjafjalla's Wand, NC Deer Fan, Nearl's Shield). Each item just
// declares its own effect/durations/NBT key; the actual activate-if-ready logic lives here ONCE
// (backed by TimedActivation, same as before), so items don't each reimplement it. This also lets
// something like PRTSItem trigger every ready skill across a player's inventory generically, without
// needing to know each item's individual activation trigger (shift-click, hold-to-charge...).
public interface ActivatableSkillItem {
    RegistryObject<MobEffect> getSkillEffect();

    int getSkillDurationTicks();

    int getSkillCooldownTicks();

    String getSkillLastUsedKey();

    // Optional -- null means no sound plays on activation.
    default RegistryObject<net.minecraft.sounds.SoundEvent> getSkillSound() {
        return ModSound.MLYNAR_POWER;
    }

    // Activates the skill if it's currently off cooldown (server-side only). Returns true if it
    // actually activated, false if it was still on cooldown -- callers that just want to fire every
    // ready skill can ignore the return value.
    default boolean activateSkill(ItemStack stack, Level level, Player player) {
        if (level.isClientSide || !TimedActivation.isReady(stack, level, getSkillLastUsedKey(), getSkillDurationTicks(), getSkillCooldownTicks())) {
            return false;
        }

        player.addEffect(new MobEffectInstance(getSkillEffect().get(), getSkillDurationTicks(), 0));
        TimedActivation.markActivated(stack, level, getSkillLastUsedKey());

        RegistryObject<net.minecraft.sounds.SoundEvent> sound = getSkillSound();
        if (sound != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), sound.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }
}
