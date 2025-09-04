package com.mod.ascendantcrafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PersistentWorkbenchBlockEntity extends BlockEntity implements Container {
    // 3x3 grid
    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    public PersistentWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        // Make sure this name matches your registration in ACBlockEntities
        super(ACBlockEntities.WORKBENCH_BE.get(), pos, state);
    }

    /* ------------ Container implementation (used by the menu wrapper) ------------ */

    @Override public int getContainerSize() { return items.size(); }

    @Override public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int index) { return items.get(index); }

    @Override public ItemStack removeItem(int index, int count) {
        ItemStack res = ContainerHelper.removeItem(items, index, count);
        if (!res.isEmpty()) setChanged();
        return res;
    }

    @Override public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = items.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        items.set(index, ItemStack.EMPTY);
        return stack;
    }

    @Override public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
        if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
        setChanged();
    }

    @Override public void setChanged() { super.setChanged(); }

    @Override public boolean stillValid(Player player) { return true; }

    @Override public void clearContent() { items.clear(); }

    /* ------------ Persistence ------------ */

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, this.items);
    }

    /* ------------ Helpers ------------ */

    /** Used by the block on break to drop contents. */
    public Container asContainerForDrops() {
        // SimpleContainer copies the array; safe to hand to drop routine
        return new SimpleContainer(items.toArray(new ItemStack[0]));
    }
}
