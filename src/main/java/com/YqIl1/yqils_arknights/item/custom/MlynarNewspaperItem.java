package com.YqIl1.yqils_arknights.item.custom;

import com.YqIl1.yqils_arknights.effect.ModEffects;
import com.YqIl1.yqils_arknights.sound.ModSound;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MlynarNewspaperItem extends Item {
    public MlynarNewspaperItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponent, TooltipFlag pIsAdvanced) {
        pTooltipComponent.add(Component.translatable("item.yqils_arknights.mlynar_newspaper.description"));
        super.appendHoverText(pStack, pLevel, pTooltipComponent, pIsAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (!pLevel.isClientSide) {
            pPlayer.addEffect(new MobEffectInstance(ModEffects.MLYNAR_POWER.get(), 600, 0));
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                    ModSound.MLYNAR_POWER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        pPlayer.getCooldowns().addCooldown(this, 1400);
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

}
