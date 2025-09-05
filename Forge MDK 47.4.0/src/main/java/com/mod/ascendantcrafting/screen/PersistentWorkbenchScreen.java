package com.mod.ascendantcrafting.screen;

import com.mod.ascendantcrafting.menu.PersistentWorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PersistentWorkbenchScreen extends AbstractContainerScreen<PersistentWorkbenchMenu> {

    private static final ResourceLocation CRAFT_TEX = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
    private static final int SLOT = 18;

    // Sidebar visuals (simple panel draws; swap to a custom PNG later if you want)
    private static final int PANEL_PADDING_X = 6;      // gap between crafting and side panel
    private static final int PANEL_INNER_PAD = 4;      // inner padding for top/bottom aesthetics
    private static final int PANEL_WIDTH = 4 * SLOT + 8;  // slots + left/right borders

    public PersistentWorkbenchScreen(PersistentWorkbenchMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        // width = vanilla 176 + gap + panel
        this.imageWidth  = 176 + PANEL_PADDING_X + PANEL_WIDTH;
        // height = vanilla 166 (we draw sidebar within that vertical space)
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos  = (this.height - this.imageHeight) / 2;

        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72; // vanilla crafting placement
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // 1) Vanilla crafting background
        g.blit(CRAFT_TEX, this.leftPos, this.topPos, 0, 0, 176, 166);

        // 2) Right-side panel background (simple rectangle; replace with PNG later)
        int panelX = this.leftPos + 176 + PANEL_PADDING_X;
        int panelY = this.topPos;
        int panelH = this.imageHeight;

        // Border
        g.fill(panelX - 1, panelY, panelX + PANEL_WIDTH + 1, panelY + panelH, 0xFF2B2B2B);
        // Inner background
        g.fill(panelX, panelY + 1, panelX + PANEL_WIDTH, panelY + panelH - 1, 0xFF3C3C3C);

        // 3) Draw a vertical scrollbar on the far-right of the sidebar
        int visibleRows = menu.getStorageVisibleRows(); // 9
        int totalRows   = menu.getStorageTotalRows();   // 14
        int maxOffset   = Math.max(0, totalRows - visibleRows); // 5

        int trackX = panelX + PANEL_WIDTH - 6;
        int trackTop = panelY + PANEL_INNER_PAD;
        int trackH = visibleRows * SLOT - (PANEL_INNER_PAD * 2); // keep knob within nice margins

        if (maxOffset == 0) {
            // draw disabled track
            g.fill(trackX, trackTop, trackX + 2, trackTop + trackH, 0x44000000);
        } else {
            int knobH = Math.max(10, (int) Math.round(trackH * (visibleRows / (double) totalRows)));
            int row = menu.getStorageRow();
            int knobTravel = trackH - knobH;
            int knobY = trackTop + (int) Math.round(knobTravel * (row / (double) maxOffset));

            g.fill(trackX, trackTop, trackX + 2, trackTop + trackH, 0x66000000);     // track
            g.fill(trackX - 1, knobY, trackX + 3, knobY + knobH, 0xFFAAAAAA);       // knob
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        g.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        // Optional: label the sidebar
        g.drawString(this.font, Component.literal("Storage"), 176 + 6 + 4, 6, 0xE0E0E0, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Only scroll when the mouse is over the side panel slots
        int panelX = this.leftPos + 176 + PANEL_PADDING_X;
        int panelY = this.topPos;

        int slotAreaLeft = panelX + 4; // inside border
        int slotAreaRight = slotAreaLeft + (4 * SLOT);
        int slotAreaTop = panelY + menu.getStoragePanelY(); // typically small pad
        int slotAreaBottom = slotAreaTop + (menu.getStorageVisibleRows() * SLOT);

        if (mouseX >= slotAreaLeft && mouseX < slotAreaRight &&
                mouseY >= slotAreaTop  && mouseY < slotAreaBottom) {
            // up = 0, down = 1
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, delta > 0 ? 0 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
