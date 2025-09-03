package com.mod.ascendantcrafting.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PersistentWorkbenchScreen extends AbstractContainerScreen<PersistentWorkbenchMenu> {
    // You can keep your previous constructor call if you like. This factory avoids the deprecation warning.
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/crafting_table.png");

    public PersistentWorkbenchScreen(PersistentWorkbenchMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEX, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Title near top-left, player inventory label above player inv grid
        g.drawString(this.font, this.title, 28, 6, 0x404040, false);
        g.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }
}

