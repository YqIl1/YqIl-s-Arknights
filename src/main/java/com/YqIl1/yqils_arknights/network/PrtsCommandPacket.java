package com.YqIl1.yqils_arknights.network;

import com.YqIl1.yqils_arknights.item.custom.PRTSItem;
import com.YqIl1.yqils_arknights.item.custom.PrtsCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

// Client -> server: a PRTS radial-menu choice. targetId = entity under the crosshair when the menu opened
// (-1 if none), pos = block position in front of the face the crosshair hit (absent if none).
public class PrtsCommandPacket {
    private final int command;
    private final int targetId;
    @Nullable
    private final BlockPos pos;

    public PrtsCommandPacket(int command, int targetId, @Nullable BlockPos pos) {
        this.command = command;
        this.targetId = targetId;
        this.pos = pos;
    }

    public static void encode(PrtsCommandPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.command);
        buf.writeVarInt(msg.targetId);
        buf.writeBoolean(msg.pos != null);
        if (msg.pos != null) buf.writeBlockPos(msg.pos);
    }

    public static PrtsCommandPacket decode(FriendlyByteBuf buf) {
        int command = buf.readVarInt();
        int targetId = buf.readVarInt();
        BlockPos pos = buf.readBoolean() ? buf.readBlockPos() : null;
        return new PrtsCommandPacket(command, targetId, pos);
    }

    public static void handle(PrtsCommandPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        PrtsCommand command = PrtsCommand.byOrdinal(msg.command);
        if (player != null && command != null) {
            PRTSItem.issue(player, command, msg.targetId, msg.pos);
        }
        ctx.get().setPacketHandled(true);
    }
}
