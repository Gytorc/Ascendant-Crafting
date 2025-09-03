package com.mod.ascendantcrafting.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AscendantCrafting.MODID);

    public static final RegistryObject<MenuType<PersistentWorkbenchMenu>> PERSISTENT_WORKBENCH_MENU =
            MENUS.register("persistent_workbench",
                    () -> IForgeMenuType.create((windowId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        BlockEntity be = inv.player.level().getBlockEntity(pos);
                        if (be instanceof PersistentWorkbenchBlockEntity pbe) {
                            return new PersistentWorkbenchMenu(windowId, inv, pbe, pos);
                        }
                        throw new IllegalStateException("Ascendant Workbench missing at " + pos);
                    }));
}

