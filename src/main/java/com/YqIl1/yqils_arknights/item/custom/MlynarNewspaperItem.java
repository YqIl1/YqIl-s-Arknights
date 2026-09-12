package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.sound.ModSound;
import com.YqIl1.yqils_arknights.util.TimedActivation;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MlynarNewspaperItem extends Item implements CrosshairCounterItem, ActivatableSkillItem {
    private static final String MLYNAR_LAST_USED_KEY = "yqils_arknights:mlynar_last_used";
    public static final int MLYNAR_DURATION_TICKS = 600; // 30s buff
    public static final int MLYNAR_COOLDOWN_SECONDS = 40;
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
        if (!level.isClientSide && entity instanceof Player player) {
            activateSkill(stack, level, player);
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

    @Override
    public RegistryObject<MobEffect> getSkillEffect() {
        return ModEffects.MLYNAR_POWER;
    }

    @Override
    public int getSkillDurationTicks() {
        return MLYNAR_DURATION_TICKS;
    }

    @Override
    public int getSkillCooldownTicks() {
        return MLYNAR_COOLDOWN_TICKS;
    }

    @Override
    public String getSkillLastUsedKey() {
        return MLYNAR_LAST_USED_KEY;
    }

    @Override
    public RegistryObject<SoundEvent> getSkillSound() {
        return ModSound.MLYNAR_POWER;
    }
}
