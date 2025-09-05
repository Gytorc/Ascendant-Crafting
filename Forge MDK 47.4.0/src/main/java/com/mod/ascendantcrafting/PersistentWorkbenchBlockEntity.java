package com.mod.ascendantcrafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block Entity for the Ascendant Workbench.
 * - Persistent 3x3 crafting grid (9 slots)
 * - Persistent storage (54 slots)
 * - Clean NBT save/load
 */
public class PersistentWorkbenchBlockEntity extends BlockEntity {

    private static final String TAG_CRAFT   = "Craft";
    private static final String TAG_STORAGE = "Storage";

    // 3x3 crafting inventory
    private final SimpleContainer craft = new SimpleContainer(9) {
        @Override public void setChanged() {
            super.setChanged();
            PersistentWorkbenchBlockEntity.this.setChanged();
        }
    };

    // 6x9 storage inventory (54 slots)
    private final SimpleContainer storage = new SimpleContainer(54) {
        @Override public void setChanged() {
            super.setChanged();
            PersistentWorkbenchBlockEntity.this.setChanged();
        }
    };

    public PersistentWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        // NOTE: Adjust ACBlockEntities.WORKBENCH_BE if your registry name differs.
        super(ACBlockEntities.WORKBENCH_BE.get(), pos, state);
    }

    /* ---------------- Crafting (3x3) helpers used by the menu wrapper ---------------- */

    public ItemStack getCraftItem(int i) { return craft.getItem(i); }

    public ItemStack removeCraftItem(int i, int count) { return craft.removeItem(i, count); }

    public ItemStack removeCraftItemNoUpdate(int i) { return craft.removeItemNoUpdate(i); }

    public void setCraftItem(int i, ItemStack stack) { craft.setItem(i, stack); }

    /** Optional access if you ever want a direct Container. */
    public Container getCraftContainer() { return craft; }

    /* ---------------- Storage (54) access used by the menu’s StorageWindow ---------------- */

    /** The menu expects a Container here; SimpleContainer is perfect. */
    public Container getStorageContainer() { return storage; }

    /* ---------------- Drops helper ---------------- */

    /**
     * Combined view of craft + storage, for clean dropping when the block is removed.
     */
    public Container asContainerForDrops() {
        int total = craft.getContainerSize() + storage.getContainerSize();
        SimpleContainer drop = new SimpleContainer(total);
        int idx = 0;
        for (int i = 0; i < craft.getContainerSize(); i++) {
            drop.setItem(idx++, craft.getItem(i).copy());
        }
        for (int i = 0; i < storage.getContainerSize(); i++) {
            drop.setItem(idx++, storage.getItem(i).copy());
        }
        return drop;
    }

    /* ---------------- NBT persistence ---------------- */

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        // Save craft
        NonNullList<ItemStack> craftList = NonNullList.withSize(craft.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < craft.getContainerSize(); i++) craftList.set(i, craft.getItem(i));
        CompoundTag craftTag = new CompoundTag();
        ContainerHelper.saveAllItems(craftTag, craftList);
        tag.put(TAG_CRAFT, craftTag);

        // Save storage
        NonNullList<ItemStack> storageList = NonNullList.withSize(storage.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < storage.getContainerSize(); i++) storageList.set(i, storage.getItem(i));
        CompoundTag storageTag = new CompoundTag();
        ContainerHelper.saveAllItems(storageTag, storageList);
        tag.put(TAG_STORAGE, storageTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        // Load craft
        NonNullList<ItemStack> craftList = NonNullList.withSize(craft.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound(TAG_CRAFT), craftList);
        for (int i = 0; i < craft.getContainerSize(); i++) craft.setItem(i, craftList.get(i));

        // Load storage
        NonNullList<ItemStack> storageList = NonNullList.withSize(storage.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound(TAG_STORAGE), storageList);
        for (int i = 0; i < storage.getContainerSize(); i++) storage.setItem(i, storageList.get(i));
    }

    /* ---------------- Misc ---------------- */

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /** If you add distance checks in your menu, this can help. */
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) return false;
        return player.distanceToSqr(
                (double) this.worldPosition.getX() + 0.5D,
                (double) this.worldPosition.getY() + 0.5D,
                (double) this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }
}
