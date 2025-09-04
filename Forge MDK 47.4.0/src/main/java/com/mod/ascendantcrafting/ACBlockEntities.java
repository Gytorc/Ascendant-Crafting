package com.mod.ascendantcrafting;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AscendantCrafting.MODID);

    // Registry id can be the block's name; the var name MUST match what you use in the BE constructor
    public static final RegistryObject<BlockEntityType<PersistentWorkbenchBlockEntity>> WORKBENCH_BE =
            BLOCK_ENTITIES.register("ascendant_workbench",
                    () -> BlockEntityType.Builder.of(
                            PersistentWorkbenchBlockEntity::new,
                            ACBlocks.ASCENDANT_WORKBENCH.get()
                    ).build(null));
}
