package com.mod.ascendantcrafting.client;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = AscendantCrafting.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ACItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AscendantCrafting.MODID);

    // BlockItem so we can place the block
    public static final RegistryObject<Item> ASCENDANT_WORKBENCH_ITEM =
            ITEMS.register("ascendant_workbench",
                    () -> new BlockItem(ACBlocks.ASCENDANT_WORKBENCH.get(), new Item.Properties()));

    // Optional sanity-check item
    public static final RegistryObject<Item> ASCENDANT_INGOT =
            ITEMS.register("ascendant_ingot", () -> new Item(new Item.Properties()));

    @SubscribeEvent
    public static void addToTabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(ASCENDANT_WORKBENCH_ITEM);
        if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) e.accept(ASCENDANT_INGOT);
    }
}
