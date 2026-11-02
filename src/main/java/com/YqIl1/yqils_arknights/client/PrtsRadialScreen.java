package com.YqIl1.yqils_arknights.client;

import com.YqIl1.yqils_arknights.item.custom.PrtsCommand;
import com.YqIl1.yqils_arknights.network.ModNetwork;
import com.YqIl1.yqils_arknights.network.PrtsCommandPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

// PRTS radial menu. Opened while right click is held (PrtsClient); point the cursor at an entry and let go of
// right click (or left click) to issue it. Esc / releasing in the middle cancels. Entries that need a target
// (attack) or a block (move) are greyed out when the crosshair had none.
public class PrtsRadialScreen extends Screen {
    private static final int RADIUS = 80;
    private static final int DEAD_ZONE = 18;
    private static final int BOX_W = 64;
    private static final int BOX_H = 20;

    // PRTS palette (see CLAUDE.md), neutrals dark -> light + accents. Same look as the Weapon Forging Station's
    // recipe list: entries bevelled like its recipe buttons, hovered entry outlined in the accent blue.
    private static final int K = 0xFF090A09, PANEL = 0xFF242624, D = 0xFF393C39, M = 0xFF767776,
            S = 0xFF999A98, G = 0xFFC8CAC7, L = 0xFFE3E5E2;
    private static final int BLUE = 0xFF00A0E8;  // hover / selection outline

    @Nullable
    private final Entity target;
    @Nullable
    private final BlockPos pos;
    private int hovered = -1;

    public PrtsRadialScreen(@Nullable Entity target, @Nullable BlockPos pos) {
        super(Component.translatable("gui.yqils_arknights.prts.title"));
        this.target = target;
        this.pos = pos;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean enabled(PrtsCommand command) {
        return (!command.needsTarget() || target != null) && (!command.needsPos() || pos != null);
    }

    // Entry i sits at -90 deg + i * (360 / n): the first one at the top, then clockwise.
    private double angleOf(int i) {
        return Math.toRadians(-90.0D + i * 360.0D / PrtsCommand.values().length);
    }

    private int pick(int mouseX, int mouseY) {
        double dx = mouseX - width / 2.0D, dy = mouseY - height / 2.0D;
        if (dx * dx + dy * dy < DEAD_ZONE * DEAD_ZONE) return -1;
        double mouse = Math.atan2(dy, dx);
        int best = -1;
        double bestDiff = Double.MAX_VALUE;
        for (int i = 0; i < PrtsCommand.values().length; i++) {
            double diff = Math.abs(Math.atan2(Math.sin(mouse - angleOf(i)), Math.cos(mouse - angleOf(i))));
            if (diff < bestDiff) {
                bestDiff = diff;
                best = i;
            }
        }
        return best >= 0 && enabled(PrtsCommand.values()[best]) ? best : -1;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        hovered = pick(mouseX, mouseY);
        int cx = width / 2, cy = height / 2;

        // Centre: what the crosshair was on.
        Component info = target != null
                ? Component.translatable("gui.yqils_arknights.prts.target", target.getDisplayName())
                : Component.translatable("gui.yqils_arknights.prts.no_target");
        int infoW = Math.max(font.width(info), font.width(title)) + 12;
        g.fill(cx - infoW / 2, cy - 14, cx + infoW / 2, cy + 14, PANEL);
        g.renderOutline(cx - infoW / 2, cy - 14, infoW, 28, K);
        g.drawCenteredString(font, title, cx, cy - 10, L);
        g.drawCenteredString(font, info, cx, cy + 2, S);

        PrtsCommand[] commands = PrtsCommand.values();
        for (int i = 0; i < commands.length; i++) {
            int bx = cx + (int) Math.round(Math.cos(angleOf(i)) * RADIUS) - BOX_W / 2;
            int by = cy + (int) Math.round(Math.sin(angleOf(i)) * RADIUS) - BOX_H / 2;
            boolean on = enabled(commands[i]);
            boolean hover = i == hovered;
            // fill, top/left edge, bottom/right edge, text
            int fill = !on ? D : hover ? G : M;
            int lightEdge = !on ? M : hover ? M : L;
            int darkEdge = hover ? L : K;
            int text = !on ? M : hover ? K : L;
            bevel(g, bx, by, BOX_W, BOX_H, fill, lightEdge, darkEdge);
            if (hover) {
                g.renderOutline(bx - 1, by - 1, BOX_W + 2, BOX_H + 2, BLUE);
            }
            Component label = Component.translatable("gui.yqils_arknights.prts." + commands[i].id);
            g.drawString(font, label, bx + (BOX_W - font.width(label)) / 2, by + (BOX_H - 8) / 2, text, false);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    // Vanilla-style bevelled box: 1 px top/left edge, 1 px bottom/right edge, fill inside.
    private static void bevel(GuiGraphics g, int x, int y, int w, int h, int fill, int topLeft, int bottomRight) {
        g.fill(x, y, x + w, y + h, fill);
        g.fill(x, y, x + w - 1, y + 1, topLeft);
        g.fill(x, y, x + 1, y + h - 1, topLeft);
        g.fill(x + 1, y + h - 1, x + w, y + h, bottomRight);
        g.fill(x + w - 1, y + 1, x + w, y + h, bottomRight);
    }

    private void confirm(int mouseX, int mouseY) {
        int choice = pick(mouseX, mouseY);
        if (choice >= 0) {
            ModNetwork.CHANNEL.sendToServer(new PrtsCommandPacket(choice, target != null ? target.getId() : -1, pos));
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }
        onClose();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1) { // right click let go -> issue whatever is under the cursor
            confirm((int) mouseX, (int) mouseY);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            confirm((int) mouseX, (int) mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
