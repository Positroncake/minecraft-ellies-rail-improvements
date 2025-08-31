package dev.elliectron.elliesrailmod.block;

import dev.elliectron.elliesrailmod.ElliesRailImprovements;
import dev.elliectron.elliesrailmod.block.custom.*;
import dev.elliectron.elliesrailmod.item.ModItems;
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
            () -> new Class1Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).requiresCorrectToolForDrops().strength(1f, 7.5f)));
    public static final DeferredBlock<Class2Rail> RAIL_CLASS_2 = registerBlock("rail_class_2",
            () -> new Class2Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).requiresCorrectToolForDrops().strength(1f, 7.5f)));
    public static final DeferredBlock<Class3Rail> RAIL_CLASS_3 = registerBlock("rail_class_3",
            () -> new Class3Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).requiresCorrectToolForDrops().strength(1f, 7.5f)));
    public static final DeferredBlock<Class4Rail> RAIL_CLASS_4 = registerBlock("rail_class_4",
            () -> new Class4Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).requiresCorrectToolForDrops().strength(2.5f, 8.5f)));
    public static final DeferredBlock<Class5Rail> RAIL_CLASS_5 = registerBlock("rail_class_5",
            () -> new Class5Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).requiresCorrectToolForDrops().strength(2.5f, 8.5f)));

    public static final DeferredBlock<PoweredClass1Rail> POWERED_RAIL_CLASS_1 = registerBlock("powered_rail_class_1",
            () -> new PoweredClass1Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).requiresCorrectToolForDrops().strength(2f, 7f)));
    public static final DeferredBlock<PoweredClass2Rail> POWERED_RAIL_CLASS_2 = registerBlock("powered_rail_class_2",
            () -> new PoweredClass2Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).requiresCorrectToolForDrops().strength(2f, 7f)));
    public static final DeferredBlock<PoweredClass3Rail> POWERED_RAIL_CLASS_3 = registerBlock("powered_rail_class_3",
            () -> new PoweredClass3Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).requiresCorrectToolForDrops().strength(2f, 7f)));
    public static final DeferredBlock<PoweredClass4Rail> POWERED_RAIL_CLASS_4 = registerBlock("powered_rail_class_4",
            () -> new PoweredClass4Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).requiresCorrectToolForDrops().strength(3.5f, 8f)));
    public static final DeferredBlock<PoweredClass5Rail> POWERED_RAIL_CLASS_5 = registerBlock("powered_rail_class_5",
            () -> new PoweredClass5Rail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).requiresCorrectToolForDrops().strength(3.5f, 8f)));
    public static final DeferredBlock<PoweredClass5HRail> POWERED_RAIL_CLASS_5H = registerBlock("powered_rail_class_5h",
            () -> new PoweredClass5HRail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).requiresCorrectToolForDrops().strength(4f, 9f)));

    public static final DeferredBlock<LinearInductionRail> LINEAR_INDUCTION_RAIL = registerBlock("linear_induction_rail",
            () -> new LinearInductionRail(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).requiresCorrectToolForDrops().strength(3f, 10f)));

    public static final DeferredBlock<Class4Intersection> INTERSECTION_CLASS_4 = registerBlock("rail_intersection",
            () -> new Class4Intersection(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).requiresCorrectToolForDrops().strength(2.5f, 7.5f)));
    public static final DeferredBlock<TrackCircuitSwitch> TRACK_CIRCUIT_SWITCH = registerBlock("track_circuit_switch",
            () -> new TrackCircuitSwitch(BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL).requiresCorrectToolForDrops().strength(2.75f, 8f)));
    public static final DeferredBlock<TrackCircuitSignal> TRACK_CIRCUIT_SIGNAL = registerBlock("track_circuit_signal",
            () -> new TrackCircuitSignal(BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL).requiresCorrectToolForDrops().strength(2.75f, 8f)));
    public static final DeferredBlock<TrackCircuitAtp> TRACK_CIRCUIT_ATP = registerBlock("track_circuit_atp",
            () -> new TrackCircuitAtp(BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL).requiresCorrectToolForDrops().strength(2.75f, 8f)));
    public static final DeferredBlock<TrackCircuitAto> TRACK_CIRCUIT_ATO = registerBlock("track_circuit_ato",
            () -> new TrackCircuitAto(BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL).requiresCorrectToolForDrops().strength(2.75f, 8f)));

    public static final DeferredBlock<Elec600VBare> ELEC_600V_BARE = registerBlock("elec_600v_bare",
            () -> new Elec600VBare(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).requiresCorrectToolForDrops().strength(2.5f, 8f)));
    public static final DeferredBlock<Elec650VBare> ELEC_650V_BARE = registerBlock("elec_650v_bare",
            () -> new Elec650VBare(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).requiresCorrectToolForDrops().strength(2.5f, 8f)));
    public static final DeferredBlock<Elec750VBare> ELEC_750V_BARE = registerBlock("elec_750v_bare",
            () -> new Elec750VBare(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).requiresCorrectToolForDrops().strength(2.5f, 8f)));
    public static final DeferredBlock<Elec25kVBare> ELEC_25kV_BARE = registerBlock("elec_25kv_bare",
            () -> new Elec25kVBare(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).requiresCorrectToolForDrops().strength(2.5f, 8f)));
    public static final DeferredBlock<Elec600VWalkway> ELEC_600V_WALKWAY = registerBlock("elec_600v_walkway",
            () -> new Elec600VWalkway(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).requiresCorrectToolForDrops().strength(3f, 12f)));
    public static final DeferredBlock<Elec650VWalkway> ELEC_650V_WALKWAY = registerBlock("elec_650v_walkway",
            () -> new Elec650VWalkway(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).requiresCorrectToolForDrops().strength(3f, 12f)));
    public static final DeferredBlock<Elec750VWalkway> ELEC_750V_WALKWAY = registerBlock("elec_750v_walkway",
            () -> new Elec750VWalkway(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).requiresCorrectToolForDrops().strength(3f, 12f)));
    public static final DeferredBlock<Elec25kVWalkway> ELEC_25kV_WALKWAY = registerBlock("elec_25kv_walkway",
            () -> new Elec25kVWalkway(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE).requiresCorrectToolForDrops().strength(3f, 12f)));
    public static final DeferredBlock<VvvfvcfGeneratorBare> VVVFVCF_GENERATOR_BARE = registerBlock("vvvfvcf_generator_bare",
            () -> new VvvfvcfGeneratorBare(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_TORCH).requiresCorrectToolForDrops().strength(3.5f, 7f)));
    public static final DeferredBlock<VvvfvcfGeneratorWalkway> VVVFVCF_GENERATOR_WALKWAY = registerBlock("vvvfvcf_generator_walkway",
            () -> new VvvfvcfGeneratorWalkway(BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_TORCH).requiresCorrectToolForDrops().strength(4f, 11f)));

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