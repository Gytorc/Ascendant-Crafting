package com.mod.ascendantcrafting;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(AscendantCrafting.MODID)
public class AscendantCrafting {
    public static final String MODID = "ascendantcrafting";

    public AscendantCrafting() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ACBlocks.BLOCKS.register(modBus);
        ACItems.ITEMS.register(modBus);
        ACBlockEntities.BLOCK_ENTITIES.register(modBus);
        ACMenus.MENUS.register(modBus);               // <-- this is the key one for your crash
        // (Register other DeferredRegisters here as you add them)
    }
}
