package dev.elliectron.elliesrailmod;

import dev.elliectron.elliesrailmod.block.ModBlocks;
import dev.elliectron.elliesrailmod.item.ModItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

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
            event.accept(ModItems.RAIL_SEGMENT);
            event.accept(ModItems.PREMIUM_RAIL_SEGMENT);
            event.accept(ModItems.POWERED_RAIL_SEGMENT);
            event.accept(ModItems.POWERED_PREMIUM_RAIL_SEGMENT);
            event.accept(ModItems.UNFINISHED_REACTION_RAIL_SEGMENT);
            event.accept(ModItems.REACTION_RAIL_SEGMENT);
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
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("ElliesRailImprovements starting");
    }
}
