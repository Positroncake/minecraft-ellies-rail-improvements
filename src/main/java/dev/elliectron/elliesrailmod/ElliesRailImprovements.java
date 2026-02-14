package dev.elliectron.elliesrailmod;

import dev.elliectron.elliesrailmod.block.ModBlocks;
import dev.elliectron.elliesrailmod.item.ModItems;
import dev.elliectron.elliesrailmod.menu.ModMenuTypes;
import dev.elliectron.elliesrailmod.recipe.ModRecipes;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ElliesRailImprovements.MODID)
public class ElliesRailImprovements {
    public static final String MODID = "elliesrailmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ElliesRailImprovements(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        NeoForge.EVENT_BUS.register(this);

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);

        ModMenuTypes.MENUS.register(modEventBus);

        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAIL_CLASS_1.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POWERED_RAIL_CLASS_1.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAIL_CLASS_2.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POWERED_RAIL_CLASS_2.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAIL_CLASS_3.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POWERED_RAIL_CLASS_3.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAIL_CLASS_4.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POWERED_RAIL_CLASS_4.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RAIL_CLASS_5.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POWERED_RAIL_CLASS_5.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POWERED_RAIL_CLASS_5H.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LINEAR_INDUCTION_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.INTERSECTION_CLASS_4.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRACK_CIRCUIT_SWITCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRACK_CIRCUIT_SIGNAL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRACK_CIRCUIT_ATP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRACK_CIRCUIT_ATO.get(), RenderType.cutout());
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModItems.SIGNAL_PROCEED);
            event.accept(ModItems.SIGNAL_OVERRIDE);
            event.accept(ModItems.SIGNAL_SWITCH_ALTERNATE);
            event.accept(ModItems.SIGNAL_STOP);
            event.accept(ModItems.SIGNAL_E_STOP);
            event.accept(ModItems.SIGNAL_SPEED_LIMITED);
            event.accept(ModItems.SIGNAL_SPEED_MEDIUM);
            event.accept(ModItems.SIGNAL_SPEED_DIVERGING);
            event.accept(ModItems.SIGNAL_SPEED_RESTRICTED);
            event.accept(ModBlocks.RAIL_CLASS_1);
            event.accept(ModBlocks.RAIL_CLASS_2);
            event.accept(ModBlocks.RAIL_CLASS_3);
            event.accept(ModBlocks.RAIL_CLASS_4);
            event.accept(ModBlocks.RAIL_CLASS_5);
            event.accept(ModBlocks.LINEAR_INDUCTION_RAIL);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_1);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_2);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_3);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_4);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_5);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_5H);
            event.accept(ModBlocks.INTERSECTION_CLASS_4);
            event.accept(ModBlocks.TRACK_CIRCUIT_SWITCH);
            event.accept(ModBlocks.TRACK_CIRCUIT_SIGNAL);
            event.accept(ModBlocks.TRACK_CIRCUIT_ATP);
            event.accept(ModBlocks.TRACK_CIRCUIT_ATO);
            event.accept(ModBlocks.ELEC_600V_BARE);
            event.accept(ModBlocks.ELEC_650V_BARE);
            event.accept(ModBlocks.ELEC_750V_BARE);
            event.accept(ModBlocks.ELEC_25kV_BARE);
            event.accept(ModBlocks.VVVFVCF_GENERATOR_BARE);
            event.accept(ModBlocks.ELEC_600V_WALKWAY);
            event.accept(ModBlocks.ELEC_650V_WALKWAY);
            event.accept(ModBlocks.ELEC_750V_WALKWAY);
            event.accept(ModBlocks.ELEC_25kV_WALKWAY);
            event.accept(ModBlocks.VVVFVCF_GENERATOR_WALKWAY);
            event.accept(ModItems.RAIL_WORKSHOP.get());
            event.accept(ModItems.SIGNAL_PROGRAMMER.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.RAIL_SEGMENT);
            event.accept(ModItems.PREMIUM_RAIL_SEGMENT);
            event.accept(ModItems.THIRD_RAIL);
            event.accept(ModItems.SINGLE_THIRD_RAIL);
            event.accept(ModItems.UNFINISHED_REACTION_RAIL_SEGMENT);
            event.accept(ModItems.REACTION_RAIL_SEGMENT);
            event.accept(ModItems.SIGNAL_COMPONENT);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.RAIL_WORKSHOP_BE.get(),
                (blockEntity, direction) -> blockEntity.getInputHandler()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.SIGNAL_PROGRAMMER_BE.get(),
                (blockEntity, direction) -> blockEntity.getInputHandler()
        );
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("ElliesRailImprovements starting");
    }
}
