package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.sound.ModSound;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// PRTS: triggers every ready ActivatableSkillItem across the player's inventory (main inventory,
// hotbar, and off hand) in one go, regardless of each item's own activation trigger.
public class PRTSItem extends Item {
    private static final int COOLDOWN_TICKS = 200; // 10s, prevents spam-triggering every skill at once

    public PRTSItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("item.yqils_arknights.prts.description"));
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            activateAllReadySkills(level, player);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSound.MLYNAR_POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void activateAllReadySkills(Level level, Player player) {
        for (ItemStack stack : player.getInventory().items) {
            tryActivate(stack, level, player);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            tryActivate(stack, level, player);
        }
    }

    private static void tryActivate(ItemStack stack, Level level, Player player) {
        if (stack.getItem() instanceof ActivatableSkillItem skillItem) {
            skillItem.activateSkill(stack, level, player);
        }
    }
}
