package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.TimedActivation;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
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

public class MlynarNewspaperItem extends Item implements CrosshairCounterItem {
    private static final String MLYNAR_LAST_USED_KEY = "yqils_arknights:mlynar_last_used";
    public static final int MLYNAR_DURATION_TICKS = 600; // 30s buff
    public static final int MLYNAR_COOLDOWN_SECONDS = 40; // additional cooldown, starts once the buff ends
    public static final int MLYNAR_COOLDOWN_TICKS = MLYNAR_COOLDOWN_SECONDS * 20;

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

        if (!TimedActivation.isReady(itemstack, pLevel, MLYNAR_LAST_USED_KEY, MLYNAR_DURATION_TICKS, MLYNAR_COOLDOWN_TICKS)) {
            return InteractionResultHolder.fail(itemstack);
        }

        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.success(itemstack);
    }


    // Only called once the full 1-second hold completes (unlike releaseUsing, which would also
    // fire on an early release) -- this is where the effect actually activates.
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player
                && TimedActivation.isReady(stack, level, MLYNAR_LAST_USED_KEY, MLYNAR_DURATION_TICKS, MLYNAR_COOLDOWN_TICKS)) {
            player.addEffect(new MobEffectInstance(ModEffects.MLYNAR_POWER.get(), MLYNAR_DURATION_TICKS, 0));
            TimedActivation.markActivated(stack, level, MLYNAR_LAST_USED_KEY);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSound.MLYNAR_POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return stack;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public static int getMlynarElapsedSeconds(ItemStack stack, Level level) {
        return TimedActivation.getElapsedSeconds(stack, level, MLYNAR_LAST_USED_KEY, MLYNAR_DURATION_TICKS, MLYNAR_COOLDOWN_SECONDS);
    }

    @Override
    public String getCrosshairCounterText(ItemStack stack, Level level) {
        return getMlynarElapsedSeconds(stack, level) + "/" + MLYNAR_COOLDOWN_SECONDS;
    }
}
