package com.mod.ascendantcrafting;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class PersistentWorkbenchBlockEntity extends BlockEntity {
    // 3×3 crafting grid
    public final ItemStackHandler grid = new ItemStackHandler(9) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    // Extra storage (2 rows for MVP)
    public final ItemStackHandler storage = new ItemStackHandler(18) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private int tier = 0;

    public PersistentWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ACBlockEntities.PERSISTENT_WORKBENCH_BE.get(), pos, state);
    }

    public int getTier() { return tier; }
    public void setTier(int t) { tier = t; setChanged(); }

    @Override protected void saveAdditional(CompoundTag tag) {
        tag.put("Grid", grid.serializeNBT());
        tag.put("Storage", storage.serializeNBT());
        tag.putInt("Tier", tier);
        super.saveAdditional(tag);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        grid.deserializeNBT(tag.getCompound("Grid"));
        storage.deserializeNBT(tag.getCompound("Storage"));
        tier = tag.getInt("Tier");
    }

    public SimpleContainer asContainerForDrops() {
        SimpleContainer sc = new SimpleContainer(grid.getSlots() + storage.getSlots());
        int i = 0;
        for (int s = 0; s < grid.getSlots(); s++) sc.setItem(i++, grid.getStackInSlot(s));
        for (int s = 0; s < storage.getSlots(); s++) sc.setItem(i++, storage.getStackInSlot(s));
        return sc;
    }
}
