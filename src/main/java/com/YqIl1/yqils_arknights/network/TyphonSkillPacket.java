package com.YqIl1.yqils_arknights.network;

import com.YqIl1.yqils_arknights.item.custom.TyphonGreatbowItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// Client -> server: the player shift + left clicked with Typhon's Greatbow in the main hand.
// Carries no data; the server re-checks the held item and sneak state itself.
public class TyphonSkillPacket {
    public static void encode(TyphonSkillPacket msg, FriendlyByteBuf buf) {
    }

    public static TyphonSkillPacket decode(FriendlyByteBuf buf) {
        return new TyphonSkillPacket();
    }

    public static void handle(TyphonSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player != null && player.isShiftKeyDown()) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof TyphonGreatbowItem bow) {
                bow.toggleSkill(stack, player.serverLevel(), player);
            }
        }
        ctx.get().setPacketHandled(true);
    }
}
