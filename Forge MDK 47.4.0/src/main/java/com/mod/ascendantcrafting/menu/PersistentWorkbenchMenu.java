package com.mod.ascendantcrafting.menu;

import com.mod.ascendantcrafting.ACMenus;
import com.mod.ascendantcrafting.PersistentWorkbenchBlockEntity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

/** Ascendant Workbench menu backed by the block entity inventory, vanilla-style live recompute. */
public class PersistentWorkbenchMenu extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;

    private final CraftingContainer craftGrid;
    private final ResultContainer result = new ResultContainer();
    private final Player player;
    private final ContainerLevelAccess access;

    public PersistentWorkbenchMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(ACMenus.ASCENDANT_WORKBENCH_MENU.get(), id);
        this.player = inv.player;
        this.access  = access;

        final PersistentWorkbenchBlockEntity be = access.evaluate((level, pos) -> {
            var e = level.getBlockEntity(pos);
            return (e instanceof PersistentWorkbenchBlockEntity pw) ? pw : null;
        }).orElse(null);

        this.craftGrid = (be != null) ? new BEBackedGrid(this, be)
                : new TransientCraftingContainer(this, 3, 3);

        // Result slot first in the menu slot list
        this.addSlot(new ResultSlot(inv.player, this.craftGrid, this.result, RESULT_SLOT, 124, 35));

        // 3×3 grid: use a slot class that always notifies the menu on any change
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                int idx = col + row * 3;
                this.addSlot(new CraftGridSlot(this, this.craftGrid, idx, 30 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory
        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 9; ++c) {
                this.addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
            }
        }
        // Hotbar
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inv, x, 8 + x * 18, 142));
        }

        // initial compute (server will push the result)
        slotsChanged(this.craftGrid);
    }

    /* ---------- live recompute like vanilla CraftingMenu ---------- */

    @Override
    public void slotsChanged(Container container) {
        if (!this.player.level().isClientSide) {
            recomputeOutput();
            // Let the regular container sync push slot changes this tick
            broadcastChanges();
        }
    }

    private void recomputeOutput() {
        var level = this.player.level();
        Optional<CraftingRecipe> recipeOpt = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, this.craftGrid, level);

        ItemStack output = ItemStack.EMPTY;
        if (recipeOpt.isPresent()) {
            CraftingRecipe recipe = recipeOpt.get();
            output = recipe.assemble(this.craftGrid, level.registryAccess());
            this.result.setRecipeUsed(level, (ServerPlayer) this.player, recipe);
        }

        this.result.setItem(RESULT_SLOT, output);
        // No need to call setRemoteSlot explicitly; broadcastChanges() will sync it.
    }

    /* ---------- standard plumbing ---------- */

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Items persist in the BE; nothing to eject here.
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();

            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(stack, 10, 46, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, copy);
            } else if (index >= 1 && index < 10) {
                if (!this.moveItemStackTo(stack, 10, 46, false)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(stack, 1, 10, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }

    /* ---------- slot with aggressive change hooks ---------- */

    private static class CraftGridSlot extends Slot {
        private final PersistentWorkbenchMenu menu;

        CraftGridSlot(PersistentWorkbenchMenu menu, Container grid, int index, int x, int y) {
            super(grid, index, x, y);
            this.menu = menu;
        }

        @Override public void setChanged() { super.setChanged(); menu.slotsChanged(this.container); }
        @Override public void set(ItemStack stack) { super.set(stack); menu.slotsChanged(this.container); }
        @Override public void onTake(Player player, ItemStack stack) { super.onTake(player, stack); menu.slotsChanged(this.container); }
    }

    /* ---------- BE-backed CraftingContainer ---------- */

    private static final class BEBackedGrid implements CraftingContainer {
        private final AbstractContainerMenu owner;
        private final PersistentWorkbenchBlockEntity be;

        BEBackedGrid(AbstractContainerMenu owner, PersistentWorkbenchBlockEntity be) {
            this.owner = owner;
            this.be = be;
        }

        @Override public int getWidth() { return 3; }
        @Override public int getHeight() { return 3; }
        @Override public int getContainerSize() { return 9; }

        @Override public boolean isEmpty() {
            for (int i = 0; i < 9; i++) if (!be.getItem(i).isEmpty()) return false;
            return true;
        }

        @Override public ItemStack getItem(int index) { return be.getItem(index); }

        @Override public ItemStack removeItem(int index, int count) {
            ItemStack res = be.removeItem(index, count);
            if (!res.isEmpty()) setChanged();
            return res;
        }

        @Override public ItemStack removeItemNoUpdate(int index) {
            ItemStack res = be.removeItemNoUpdate(index);
            if (!res.isEmpty()) setChanged();
            return res;
        }

        @Override public void setItem(int index, ItemStack stack) {
            be.setItem(index, stack);
            setChanged();
        }

        @Override public void setChanged() {
            owner.slotsChanged(this); // immediate recompute
            be.setChanged();          // mark BE dirty for saving
        }

        @Override public boolean stillValid(Player player) { return true; }

        @Override public void clearContent() {
            for (int i = 0; i < 9; i++) be.setItem(i, ItemStack.EMPTY);
            setChanged();
        }

        @Override public void fillStackedContents(StackedContents contents) {
            for (int i = 0; i < 9; i++) contents.accountSimpleStack(be.getItem(i));
        }

        @Override public List<ItemStack> getItems() {
            List<ItemStack> list = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) list.add(be.getItem(i));
            return list;
        }
    }
}
