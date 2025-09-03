package com.mod.ascendantcrafting.client;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ACBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AscendantCrafting.MODID);

    public static final RegistryObject<BlockEntityType<PersistentWorkbenchBlockEntity>> PERSISTENT_WORKBENCH_BE =
            BLOCK_ENTITIES.register("persistent_workbench",
                    () -> BlockEntityType.Builder.of(PersistentWorkbenchBlockEntity::new, ACBlocks.ASCENDANT_WORKBENCH.get()).build(null));
}