package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.item.custom.TyphonGreatbowItem;
import com.YqIl1.yqils_arknights.network.ModNetwork;
import com.YqIl1.yqils_arknights.network.TyphonSkillPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Shift + left click with Typhon's Greatbow toggles "Eternal Hunt". The attack key fires this event whether the
// crosshair is on air, a block or an entity, so we catch it here and tell the server, instead of
// letting the click break a block or hit something.
@Mod.EventBusSubscriber(modid = Yqils_arknights.MODID, value = Dist.CLIENT)
public final class TyphonGreatbowInput {
    private TyphonGreatbowInput() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isShiftKeyDown()) return;
        if (!(player.getMainHandItem().getItem() instanceof TyphonGreatbowItem)) return;

        ModNetwork.CHANNEL.sendToServer(new TyphonSkillPacket());
        event.setCanceled(true);
        event.setSwingHand(true);
    }
}
