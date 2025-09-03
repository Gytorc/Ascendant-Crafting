package com.mod.ascendantcrafting.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;

public class PersistentWorkbenchMenu extends CraftingMenu {
    private final BlockPos blockPos;

    public PersistentWorkbenchMenu(int windowId, Inventory playerInv, BlockPos pos) {
        // Standard 3x3 crafting table menu (vanilla slots, result, shift-click, etc.)
        super(windowId, playerInv);
        this.blockPos = pos;
    }

    @Override
    public boolean stillValid(Player player) {
        // For now: allow while open (or do a distance check to pos if you prefer)
        return true;
    }
}
