package com.mod.ascendantcrafting.client;

import com.mod.ascendantcrafting.ACMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Run on the render thread after MC is ready for client bindings
        event.enqueueWork(() -> {
            MenuScreens.register(ACMenus.PERSISTENT_WORKBENCH_MENU.get(),
                    PersistentWorkbenchScreen::new);
        });
    }
}
