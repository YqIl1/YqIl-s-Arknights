package com.YqIl1.yqils_arknights.network;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// The mod's one network channel. Register packets in register(), called from commonSetup.
public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Yqils_arknights.MODID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int nextId = 0;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(TyphonSkillPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TyphonSkillPacket::encode)
                .decoder(TyphonSkillPacket::decode)
                .consumerMainThread(TyphonSkillPacket::handle)
                .add();
    }
}
