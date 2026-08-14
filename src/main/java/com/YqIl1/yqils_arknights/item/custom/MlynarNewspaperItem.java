package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.sound.ModSound;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MlynarNewspaperItem extends Item {
    private static final String MLYNAR_LAST_USED_KEY = "yqils_arknights:mlynar_last_used";
    public static final int MLYNAR_COOLDOWN_SECONDS = 40;
    public static final int MLYNAR_COOLDOWN_TICKS = MLYNAR_COOLDOWN_SECONDS * 20; //  40s cooldown

    public MlynarNewspaperItem(Properties pProperties) {
        super(pProperties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponent, TooltipFlag pIsAdvanced) {
        pTooltipComponent.add(Component.translatable("item.yqils_arknights.mlynar_newspaper.description"));
        super.appendHoverText(pStack, pLevel, pTooltipComponent, pIsAdvanced);
    }

    // 1 second (20 ticks) hold-to-activate -- reaching the full duration triggers finishUsingItem
    // below; letting go early does nothing (no releaseUsing override), so a premature release just
    // cancels the attempt.
    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    // Without this, getUseAnimation() defaults to UseAnim.NONE, so PlayerRenderer shows no special
    // pose during the hold -- no visible feedback that anything's happening.
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (!pLevel.isClientSide) {
            CompoundTag tag = itemstack.getOrCreateTag();
            long lastUsed = tag.getLong(MLYNAR_LAST_USED_KEY);
            long elapsed = pLevel.getGameTime() - lastUsed;
            if (elapsed < MLYNAR_COOLDOWN_TICKS) {
                return InteractionResultHolder.fail(itemstack);
            }
        }

        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    // Only called once the full 1-second hold completes (unlike releaseUsing, which would also
    // fire on an early release) -- this is where the effect actually activates.
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            long lastUsed = tag.getLong(MLYNAR_LAST_USED_KEY);
            long elapsed = level.getGameTime() - lastUsed;

            if (elapsed >= MLYNAR_COOLDOWN_TICKS) {
                player.addEffect(new MobEffectInstance(ModEffects.MLYNAR_POWER.get(), 600, 0));
                tag.putLong(MLYNAR_LAST_USED_KEY, level.getGameTime());
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSound.MLYNAR_POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        if (!player.hasEffect(ModEffects.MLYNAR_POWER.get())) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(MLYNAR_LAST_USED_KEY)) {
            return;
        }

        long lastUsed = tag.getLong(MLYNAR_LAST_USED_KEY);
        long elapsed = level.getGameTime() - lastUsed;
        if (elapsed < MLYNAR_COOLDOWN_TICKS) {
            tag.putLong(MLYNAR_LAST_USED_KEY, lastUsed + 1);
        }
    }

    public static int getMlynarElapsedSeconds(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return MLYNAR_COOLDOWN_SECONDS;
        long elapsedTicks = level.getGameTime() - tag.getLong(MLYNAR_LAST_USED_KEY);
        long elapsedSeconds = elapsedTicks / 20;
        return (int) Math.min(elapsedSeconds, MLYNAR_COOLDOWN_SECONDS);
    }
}
