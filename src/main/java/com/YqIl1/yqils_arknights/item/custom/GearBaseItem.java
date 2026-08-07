package com.YqIl1.yqils_arknights.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GearBaseItem extends Item {
    public GearBaseItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> componentList, TooltipFlag tooltipFlag) {
        componentList.add(Component.translatable("item.yqils_arknights.gear_base.description"));
        super.appendHoverText(itemStack, level, componentList, tooltipFlag);
    }
}
