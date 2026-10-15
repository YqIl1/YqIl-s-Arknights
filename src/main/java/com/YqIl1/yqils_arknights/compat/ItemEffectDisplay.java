package com.YqIl1.yqils_arknights.compat;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.item.ModItems;
import com.YqIl1.yqils_arknights.item.custom.IfritFlamethrowerItem;
import com.YqIl1.yqils_arknights.item.custom.LapplandBladeItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// Display-only pairing shown by both the JEI category (compat/ItemEffectCategory.java) and the EMI
// recipe (compat/ItemEffectEmiRecipe.java) -- not a real in-game recipe, just tells the recipe viewer
// "using this item is connected to this effect" so it shows up under the item's own lookup.
// relation distinguishes whether the item grants the effect to its wielder (GAIN) or applies it to
// whatever it hits (APPLY); both render on the same "Item Effects" page with a different subtitle.
public record ItemEffectDisplay(ItemStack item, MobEffect effect, int durationTicks, Relation relation) {
    public enum Relation {
        GAIN,
        APPLY
    }

    // Single source of truth for both YqilsArknightsJeiPlugin and YqilsArknightsEmiPlugin -- durations
    // here mirror the addEffect(...) calls in each item's own use()/finishUsingItem() logic.
    public static List<ItemEffectDisplay> all() {
        return List.of(
                new ItemEffectDisplay(new ItemStack(ModItems.LAPPLAND_BLADE.get()), ModEffects.LAPPLAND_BLADE_POWER.get(), LapplandBladeItem.POWER_EFFECT_DURATION_TICKS, Relation.GAIN),
                new ItemEffectDisplay(new ItemStack(ModItems.LAPPLAND_BLADE.get()), ModEffects.FRAGILE.get(), 160, Relation.APPLY),
                new ItemEffectDisplay(new ItemStack(ModItems.LAPPLAND_BLADE.get()), ModEffects.SILENCE.get(), LapplandBladeItem.SILENCE_DURATION_TICKS, Relation.APPLY),
                new ItemEffectDisplay(new ItemStack(ModItems.MLYNAR_NEWSPAPER.get()), ModEffects.MLYNAR_POWER.get(), 600, Relation.GAIN),
                new ItemEffectDisplay(new ItemStack(ModItems.EYJAFJALLA_WAND.get()), ModEffects.EYJAFJALLA_WAND_POWER.get(), 300, Relation.GAIN),
                new ItemEffectDisplay(new ItemStack(ModItems.NCDEER_FAN.get()), ModEffects.NC_DEER_FAN_POWER.get(), 400, Relation.GAIN),
                new ItemEffectDisplay(new ItemStack(ModItems.IFRIT_FLAMETHROWER.get()), ModEffects.IMMOLATION.get(), IfritFlamethrowerItem.IMMOLATION_DURATION_TICKS, Relation.APPLY),
                new ItemEffectDisplay(new ItemStack(ModItems.NEARL_SHIELD.get()), ModEffects.NEARL_SHIELD_POWER.get(), 800, Relation.GAIN),
                new ItemEffectDisplay(new ItemStack(ModItems.TEXAS_BLADE.get()), ModEffects.STUN.get(), 60, Relation.APPLY),
                new ItemEffectDisplay(new ItemStack(ModItems.CEOBE_SPEAR.get()), ModEffects.CEOBE_SPEAR_POWER.get(),
                        com.YqIl1.yqils_arknights.item.custom.CeobeSpearItem.SKILL_DURATION_TICKS, Relation.GAIN),
                new ItemEffectDisplay(new ItemStack(ModItems.CEOBE_SPEAR.get()), ModEffects.SILENCE.get(),
                        com.YqIl1.yqils_arknights.item.custom.CeobeSpearItem.SILENCE_DURATION_TICKS, Relation.APPLY)
        );
    }
}
