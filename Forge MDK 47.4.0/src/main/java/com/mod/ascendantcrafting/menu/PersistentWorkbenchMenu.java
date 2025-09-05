package com.mod.ascendantcrafting.menu;

import com.mod.ascendantcrafting.ACMenus;
import com.mod.ascendantcrafting.PersistentWorkbenchBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Ascendant Workbench menu with a 4-column side storage.
 * - Crafting area uses vanilla coordinates (176x166 texture).
 * - Storage side panel is a 4-wide, vertically scrolling window over 54 slots.
 */
public class PersistentWorkbenchMenu extends AbstractContainerMenu {

    // ---- Vanilla crafting layout (left panel) ----
    private static final int GUI_WIDTH_BASE = 176;
    private static final int SLOT = 18;

    private static final int RESULT_X = 124, RESULT_Y = 35;
    private static final int GRID_START_X = 30, GRID_START_Y = 17;

    private static final int PLAYER_INV_START_X = 8, PLAYER_INV_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    // ---- Side storage layout (right panel) ----
    private static final int STORAGE_COLS = 4;              // 4-wide sidebar
    private static final int STORAGE_TOTAL = 54;            // backing size in BE
    private static final int STORAGE_TOTAL_ROWS = (STORAGE_TOTAL + STORAGE_COLS - 1) / STORAGE_COLS; // ceil(54/4)=14
    private static final int STORAGE_VISIBLE_ROWS = 9;      // fits within 166px (9*18=162)
    private static final int STORAGE_VISIBLE_SLOTS = STORAGE_COLS * STORAGE_VISIBLE_ROWS; // 36
    // Where to place the sidebar relative to the left of the vanilla 176px panel
    private static final int STORAGE_PANEL_X = GUI_WIDTH_BASE + 6; // 6px gap to the right of crafting
    private static final int STORAGE_PANEL_Y = 4;                  // small top padding

    // ---- Slot index ranges (for quickMoveStack) ----
    private static final int SLOT_RESULT            = 0;
    private static final int SLOT_GRID_FIRST        = 1;                                  // 1..9 (exclusive end below)
    private static final int SLOT_GRID_LAST         = SLOT_GRID_FIRST + 9;               // 10
    private static final int SLOT_STORAGE_FIRST     = SLOT_GRID_LAST;                    // 10..(10+36)
    private static final int SLOT_STORAGE_LAST      = SLOT_STORAGE_FIRST + STORAGE_VISIBLE_SLOTS; // 46
    private static final int SLOT_PLAYER_FIRST      = SLOT_STORAGE_LAST;                 // 46..73
    private static final int SLOT_PLAYER_LAST       = SLOT_PLAYER_FIRST + 27;            // 73
    private static final int SLOT_HOTBAR_FIRST      = SLOT_PLAYER_LAST;                  // 73..82
    private static final int SLOT_HOTBAR_LAST       = SLOT_HOTBAR_FIRST + 9;             // 82

    private final Player player;
    private final ContainerLevelAccess access;

    private final CraftingContainer craftGrid;
    private final ResultContainer result = new ResultContainer();

    /** Window (36 slots) into the 54-slot backing storage. */
    private final StorageWindow storageWindow;

    /** Row offset (0..maxRow). Synced via DataSlot. */
    private int rowOffset = 0;
    private final DataSlot rowOffsetTracker = new DataSlot() {
        @Override public int get() { return rowOffset; }
        @Override public void set(int value) {
            int maxRow = Math.max(0, STORAGE_TOTAL_ROWS - STORAGE_VISIBLE_ROWS); // 14-9=5
            rowOffset = Mth.clamp(value, 0, maxRow);
            storageWindow.setRowOffset(rowOffset);
        }
    };

    public PersistentWorkbenchMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(ACMenus.ASCENDANT_WORKBENCH_MENU.get(), id);
        this.player = inv.player;
        this.access = access;

        // Resolve BE (both sides)
        final PersistentWorkbenchBlockEntity be = access.evaluate((level, pos) -> {
            var e = level.getBlockEntity(pos);
            return (e instanceof PersistentWorkbenchBlockEntity pw) ? pw : null;
        }).orElse(null);

        // Crafting 3x3
        this.craftGrid = (be != null) ? new BEBackedGrid(this, be) : new TransientCraftingContainer(this, 3, 3);

        // Storage window over BE storage (or client fallback)
        Container backing = (be != null) ? be.getStorageContainer() : new SimpleContainer(STORAGE_TOTAL);
        this.storageWindow = new StorageWindow(backing);

        // --- SLOTS ---

        // Result
        this.addSlot(new ResultSlot(inv.player, this.craftGrid, this.result, SLOT_RESULT, RESULT_X, RESULT_Y));

        // 3×3 grid
        int idx = 0;
        for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++, idx++) {
            this.addSlot(new Slot(this.craftGrid, idx, GRID_START_X + c * SLOT, GRID_START_Y + r * SLOT));
        }

        // Sidebar storage slots (4 columns × 9 visible rows)
        for (int r = 0; r < STORAGE_VISIBLE_ROWS; r++) {
            for (int c = 0; c < STORAGE_COLS; c++) {
                int i = r * STORAGE_COLS + c; // 0..35
                this.addSlot(new Slot(this.storageWindow, i,
                        STORAGE_PANEL_X + c * SLOT,
                        STORAGE_PANEL_Y + r * SLOT));
            }
        }

        // Player inventory (27)
        for (int r2 = 0; r2 < 3; r2++) {
            for (int c2 = 0; c2 < 9; c2++) {
                this.addSlot(new Slot(inv, c2 + r2 * 9 + 9,
                        8 + c2 * SLOT, 84 + r2 * SLOT));
            }
        }
        // Hotbar (9)
        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(inv, x, 8 + x * SLOT, HOTBAR_Y));
        }

        // Sync rowOffset to client
        this.addDataSlot(rowOffsetTracker);
        this.rowOffsetTracker.set(0);

        // Initial result
        this.slotsChanged(this.craftGrid);
    }

    // Exposed for the Screen
    public int getStorageRow() { return rowOffset; }
    public int getStorageVisibleRows() { return STORAGE_VISIBLE_ROWS; }
    public int getStorageTotalRows() { return STORAGE_TOTAL_ROWS; }
    public int getStorageCols() { return STORAGE_COLS; }
    public int getStoragePanelX() { return STORAGE_PANEL_X; }
    public int getStoragePanelY() { return STORAGE_PANEL_Y; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        // 0 = scroll up, 1 = scroll down
        if (id == 0 || id == 1) {
            int maxRow = Math.max(0, STORAGE_TOTAL_ROWS - STORAGE_VISIBLE_ROWS);
            int next = Mth.clamp(rowOffset + (id == 0 ? -1 : +1), 0, maxRow);
            if (next != rowOffset) rowOffsetTracker.set(next);
            return true;
        }
        return false;
    }

    @Override
    public void slotsChanged(Container container) {
        if (this.player.level().isClientSide) return;

        var level = this.player.level();
        Optional<CraftingRecipe> rec = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, this.craftGrid, level);

        ItemStack out = ItemStack.EMPTY;
        if (rec.isPresent()) {
            CraftingRecipe r = rec.get();
            out = r.assemble(this.craftGrid, level.registryAccess());
            this.result.setRecipeUsed(level, (ServerPlayer) this.player, r);
        }

        this.result.setItem(SLOT_RESULT, out);
        this.setRemoteSlot(SLOT_RESULT, out);
        this.broadcastChanges();
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();

            if (index == SLOT_RESULT) {
                if (!this.moveItemStackTo(stack, SLOT_PLAYER_FIRST, SLOT_HOTBAR_LAST, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, copy);
            }
            else if (index >= SLOT_GRID_FIRST && index < SLOT_GRID_LAST) {
                if (!this.moveItemStackTo(stack, SLOT_PLAYER_FIRST, SLOT_HOTBAR_LAST, false)) return ItemStack.EMPTY;
            }
            else if (index >= SLOT_STORAGE_FIRST && index < SLOT_STORAGE_LAST) {
                if (!this.moveItemStackTo(stack, SLOT_PLAYER_FIRST, SLOT_HOTBAR_LAST, false)) return ItemStack.EMPTY;
            }
            else if (index >= SLOT_PLAYER_FIRST && index < SLOT_HOTBAR_LAST) {
                // Prefer putting into storage window first
                if (!this.moveItemStackTo(stack, SLOT_STORAGE_FIRST, SLOT_STORAGE_LAST, false)) {
                    // then try grid
                    if (!this.moveItemStackTo(stack, SLOT_GRID_FIRST, SLOT_GRID_LAST, false)) {
                        // then swap between player inv/hotbar
                        if (index < SLOT_HOTBAR_FIRST) {
                            if (!this.moveItemStackTo(stack, SLOT_HOTBAR_FIRST, SLOT_HOTBAR_LAST, false)) return ItemStack.EMPTY;
                        } else if (!this.moveItemStackTo(stack, SLOT_PLAYER_FIRST, SLOT_PLAYER_LAST, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }

    // ---------- Inner classes ----------

    /** 3×3 CraftingContainer backed by the BE. */
    private static final class BEBackedGrid implements CraftingContainer {
        private final AbstractContainerMenu owner;
        private final PersistentWorkbenchBlockEntity be;
        BEBackedGrid(AbstractContainerMenu owner, PersistentWorkbenchBlockEntity be) {
            this.owner = owner; this.be = be;
        }
        @Override public int getWidth() { return 3; }
        @Override public int getHeight() { return 3; }
        @Override public int getContainerSize() { return 9; }
        @Override public boolean isEmpty() {
            for (int i = 0; i < 9; i++) if (!be.getCraftItem(i).isEmpty()) return false;
            return true;
        }
        @Override public ItemStack getItem(int i) { return be.getCraftItem(i); }
        @Override public ItemStack removeItem(int i, int count) {
            ItemStack res = be.removeCraftItem(i, count);
            if (!res.isEmpty()) setChanged();
            return res;
        }
        @Override public ItemStack removeItemNoUpdate(int i) {
            ItemStack res = be.removeCraftItemNoUpdate(i);
            if (!res.isEmpty()) setChanged();
            return res;
        }
        @Override public void setItem(int i, ItemStack stack) {
            be.setCraftItem(i, stack);
            setChanged();
        }
        @Override public void setChanged() { owner.slotsChanged(this); be.setChanged(); }
        @Override public boolean stillValid(Player p) { return true; }
        @Override public void clearContent() { for (int i = 0; i < 9; i++) be.setCraftItem(i, ItemStack.EMPTY); setChanged(); }
        @Override public void fillStackedContents(StackedContents c) { for (int i = 0; i < 9; i++) c.accountSimpleStack(be.getCraftItem(i)); }
        @Override public List<ItemStack> getItems() {
            List<ItemStack> list = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) list.add(be.getCraftItem(i));
            return list;
        }
    }

    /**
     * 36-slot window over a 54-slot backing container, arranged in 4 columns × 9 visible rows.
     * Out-of-range slots (past 54) behave as empty/no-op.
     */
    private static final class StorageWindow implements Container {
        private final Container backing;
        private int rowOffset = 0; // 0..(STORAGE_TOTAL_ROWS - STORAGE_VISIBLE_ROWS)
        StorageWindow(Container backing) { this.backing = backing; }
        void setRowOffset(int row) { this.rowOffset = Mth.clamp(row, 0, Math.max(0, STORAGE_TOTAL_ROWS - STORAGE_VISIBLE_ROWS)); }
        private int base() { return rowOffset * STORAGE_COLS; }

        private int mapIndex(int visibleIndex) {
            int linear = base() + visibleIndex; // 0..(54-1) valid
            return (linear >= 0 && linear < STORAGE_TOTAL) ? linear : -1;
        }

        @Override public int getContainerSize() { return STORAGE_VISIBLE_SLOTS; }

        @Override public boolean isEmpty() {
            for (int i = 0; i < STORAGE_VISIBLE_SLOTS; i++) if (!getItem(i).isEmpty()) return false;
            return true;
        }

        @Override public ItemStack getItem(int index) {
            int m = mapIndex(index);
            return m < 0 ? ItemStack.EMPTY : backing.getItem(m);
        }

        @Override public ItemStack removeItem(int index, int count) {
            int m = mapIndex(index);
            ItemStack res = (m < 0) ? ItemStack.EMPTY : backing.removeItem(m, count);
            setChanged();
            return res;
        }

        @Override public ItemStack removeItemNoUpdate(int index) {
            int m = mapIndex(index);
            ItemStack res = (m < 0) ? ItemStack.EMPTY : backing.removeItemNoUpdate(m);
            setChanged();
            return res;
        }

        @Override public void setItem(int index, ItemStack stack) {
            int m = mapIndex(index);
            if (m >= 0) backing.setItem(m, stack);
            setChanged();
        }

        @Override public void setChanged() { backing.setChanged(); }
        @Override public boolean stillValid(Player p) { return true; }
        @Override public void clearContent() {
            for (int i = 0; i < STORAGE_VISIBLE_SLOTS; i++) {
                int m = mapIndex(i);
                if (m >= 0) backing.setItem(m, ItemStack.EMPTY);
            }
            setChanged();
        }
    }
}
