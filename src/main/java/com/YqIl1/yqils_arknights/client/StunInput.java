package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.Yqils_arknights;
import com.YqIl1.yqils_arknights.effect.ModEffects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Stun on players (client side): while the local player is stunned, gameplay input does nothing --
// no moving/jumping/sneaking/sprinting, no attack/use/pick-block, no hotbar switching (scroll or number
// keys), no dropping (Q) or offhand swap (F). The camera, chat, inventory, Esc and F-keys still work.
// The server also refuses attacks/interactions/block changes (ModEventHandlers), in case a client skips this.
@Mod.EventBusSubscriber(modid = Yqils_arknights.MODID, value = Dist.CLIENT)
public final class   StunInput {
    private StunInput() {
    }

    private static boolean localPlayerStunned() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.hasEffect(ModEffects.STUN.get());
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!event.getEntity().hasEffect(ModEffects.STUN.get())) return;
        Input input = event.getInput();
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
        event.getEntity().setSprinting(false);
    }

    // Attack / use / pick block -- covers clicking, holding to mine, and holding right click.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (localPlayerStunned()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null && localPlayerStunned()) {
            event.setCanceled(true); // only in-world scrolling (hotbar); scrolling inside menus still works
        }
    }

    // Keys vanilla handles itself without a Forge event: swallow their presses before Minecraft#handleKeybinds
    // (which runs later in the same client tick). Also let go of anything already being used (bow, food...).
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !localPlayerStunned()) return;
        Minecraft mc = Minecraft.getInstance();
        drain(mc.options.keyDrop);
        drain(mc.options.keySwapOffhand);
        for (KeyMapping hotbar : mc.options.keyHotbarSlots) {
            drain(hotbar);
        }
        if (mc.player.isUsingItem() && mc.gameMode != null) {
            mc.gameMode.releaseUsingItem(mc.player);
        }
    }

    private static void drain(KeyMapping key) {
        while (key.consumeClick()) {
            // discard
        }
    }
}
