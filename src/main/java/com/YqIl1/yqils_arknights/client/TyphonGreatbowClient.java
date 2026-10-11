package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.item.custom.TyphonGreatbowItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;

// Client-only helper for the greatbow's animation controller -- kept out of the item class so the
// item never touches client classes directly.
public final class TyphonGreatbowClient {
    private TyphonGreatbowClient() {
    }

    // True if any player is currently drawing this bow. Matches the exact stack instance first, then
    // falls back to GeckoLib's per-stack id, in case the rendered stack and the use stack are different
    // copies (e.g. right after a server sync replaced the stack).
    public static boolean isBeingDrawn(ItemStack stack) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;

        for (AbstractClientPlayer player : level.players()) {
            if (!player.isUsingItem()) continue;
            ItemStack using = player.getUseItem();
            if (!(using.getItem() instanceof TyphonGreatbowItem)) continue;

            long id = GeoItem.getId(stack);
            if (using == stack
                    || player.getItemInHand(player.getUsedItemHand()) == stack
                    || (id != Long.MAX_VALUE && id == GeoItem.getId(using))) {
                return true;
            }
        }
        return false;
    }
}
