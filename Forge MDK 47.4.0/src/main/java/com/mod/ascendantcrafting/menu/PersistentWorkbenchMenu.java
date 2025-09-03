package com.mod.ascendantcrafting.menu;

import com.mod.ascendantcrafting.ACMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;

public class PersistentWorkbenchMenu extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;

    private final TransientCraftingContainer craftGrid = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer result = new ResultContainer();
    private final Player player;
    private final ContainerLevelAccess access;

    public PersistentWorkbenchMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(ACMenus.ASCENDANT_WORKBENCH_MENU.get(), id);
        this.player = inv.player;
        this.access = access;

        // Result slot
        this.addSlot(new ResultSlot(inv.player, this.craftGrid, this.result, RESULT_SLOT, 124, 35));

        // 3×3 grid slots (vanilla crafting layout)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                this.addSlot(new Slot(this.craftGrid, col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory (27)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar (9)
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(inv, x, 8 + x * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (this.player.level().isClientSide) return;

        var level = this.player.level();
        Optional<CraftingRecipe> recipeOpt = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, this.craftGrid, level);

        ItemStack output = ItemStack.EMPTY;
        if (recipeOpt.isPresent()) {
            CraftingRecipe recipe = recipeOpt.get();
            if (this.result.setRecipeUsed(level, (ServerPlayer) this.player, recipe)) {
                // If the recipe tracks usage, it’s set here.
            }
            output = recipe.assemble(this.craftGrid, level.registryAccess());
        }

        this.result.setItem(RESULT_SLOT, output);
        this.setRemoteSlot(RESULT_SLOT, output);
        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        // If you want range checks use `access.evaluate(...)`.
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // For now (no persistence yet), return inputs if the menu closes
        this.clearContainer(player, this.craftGrid);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();

            // Result slot -> player inventory
            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(stack, 10, 46, true)) return ItemStack.EMPTY; // 10..46 = player inv + hotbar
                slot.onQuickCraft(stack, copy);
            }
            // Grid slots -> player inventory
            else if (index >= 1 && index < 10) {
                if (!this.moveItemStackTo(stack, 10, 46, false)) return ItemStack.EMPTY;
            }
            // Player inventory -> grid
            else if (!this.moveItemStackTo(stack, 1, 10, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }
}
