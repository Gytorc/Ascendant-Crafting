package com.mod.ascendantcrafting;

import com.mod.ascendantcrafting.AscendantCrafting; // for MODID
import com.mod.ascendantcrafting.menu.PersistentWorkbenchMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ACMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, "ascendantcrafting");

    public static final RegistryObject<MenuType<PersistentWorkbenchMenu>> ASCENDANT_WORKBENCH_MENU =
            MENUS.register("ascendant_workbench",
                    () -> IForgeMenuType.create((windowId, inv, buf) ->
                            new PersistentWorkbenchMenu(windowId, inv,
                                    // If you want position context later:
                                    // ContainerLevelAccess.NULL or read BlockPos from buf
                                    net.minecraft.world.inventory.ContainerLevelAccess.NULL)));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}

