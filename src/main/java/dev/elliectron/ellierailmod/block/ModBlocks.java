package dev.elliectron.ellierailmod.block;

import dev.elliectron.ellierailmod.ElliesRailImprovements;
import dev.elliectron.ellierailmod.block.custom.*;
import dev.elliectron.ellierailmod.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ElliesRailImprovements.MODID);

    public static final DeferredBlock<Class1Rail> RAIL_CLASS_1 = registerBlock("rail_class_1",
            () -> new Class1Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)));
    public static final DeferredBlock<PoweredClass1Rail> POWERED_RAIL_CLASS_1 = registerBlock("powered_rail_class_1",
            () -> new PoweredClass1Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)));

    public static final DeferredBlock<Class2Rail> RAIL_CLASS_2 = registerBlock("rail_class_2",)

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}