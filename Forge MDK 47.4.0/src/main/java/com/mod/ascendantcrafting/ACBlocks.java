package com.mod.ascendantcrafting.client;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AscendantCrafting.MODID);

    public static final RegistryObject<Block> ASCENDANT_WORKBENCH =
            BLOCKS.register("ascendant_workbench",
                    () -> new PersistentWorkbenchBlock(BlockBehaviour.Properties
                            .of().mapColor(MapColor.WOOD)
                            .strength(2.5F)
                            .sound(SoundType.WOOD)));
}
