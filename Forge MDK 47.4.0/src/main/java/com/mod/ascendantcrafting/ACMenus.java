package com.mod.ascendantcrafting;

import com.mod.ascendantcrafting.menu.PersistentWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AscendantCrafting.MODID);

    public static final RegistryObject<MenuType<PersistentWorkbenchMenu>> ASCENDANT_WORKBENCH_MENU =
            MENUS.register("ascendant_workbench", () ->
                    IForgeMenuType.create((id, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        return new PersistentWorkbenchMenu(
                                id,
                                inv,
                                ContainerLevelAccess.create(inv.player.level(), pos)
                        );
                    })
            );
}
