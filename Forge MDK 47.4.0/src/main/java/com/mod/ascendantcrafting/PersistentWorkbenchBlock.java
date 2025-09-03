package com.mod.ascendantcrafting;

import com.mod.ascendantcrafting.menu.PersistentWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class PersistentWorkbenchBlock extends Block implements EntityBlock {

    public PersistentWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PersistentWorkbenchBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PersistentWorkbenchBlockEntity workbench) {
                MenuProvider provider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("block.ascendantcrafting.ascendant_workbench");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                        // Pass level+pos via ContainerLevelAccess to match the menu constructor
                        return new PersistentWorkbenchMenu(id, inv, ContainerLevelAccess.create(level, pos));
                    }
                };
                // Sends BlockPos to client; your IForgeMenuType factory can read it (buf.readBlockPos)
                NetworkHooks.openScreen(sp, provider, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PersistentWorkbenchBlockEntity workbench) {
                // Drop contents stored in the workbench
                Containers.dropContents(level, pos, workbench.asContainerForDrops());
            }
        }
        super.onRemove(oldState, level, pos, newState, movedByPiston);
    }
}
