package com.mod.ascendantcrafting.client;

import com.mod.ascendantcrafting.ACMenus;
import com.mod.ascendantcrafting.AscendantCrafting;
import com.mod.ascendantcrafting.screen.PersistentWorkbenchScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AscendantCrafting.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() ->
                MenuScreens.register(ACMenus.ASCENDANT_WORKBENCH_MENU.get(), PersistentWorkbenchScreen::new)
        );
    }
}
