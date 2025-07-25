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
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.POWERED_RAIL_CLASS_5A.get(), RenderType.cutout());
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModItems.SIGNAL_PROCEED);
            event.accept(ModItems.SIGNAL_OVERRIDE);
            event.accept(ModItems.SIGNAL_STOP);
            event.accept(ModItems.SIGNAL_E_STOP);
            event.accept(ModItems.SIGNAL_SWITCH_ALTERNATE);
            event.accept(ModItems.SIGNAL_SPEED_LIMITED);
            event.accept(ModItems.SIGNAL_SPEED_MEDIUM);
            event.accept(ModItems.SIGNAL_SPEED_DIVERGING);
            event.accept(ModItems.SIGNAL_SPEED_RESTRICTED);
            event.accept(ModBlocks.RAIL_CLASS_1);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_1);
            event.accept(ModBlocks.RAIL_CLASS_2);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_2);
            event.accept(ModBlocks.RAIL_CLASS_3);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_3);
            event.accept(ModBlocks.RAIL_CLASS_4);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_4);
            event.accept(ModBlocks.RAIL_CLASS_5);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_5);
            event.accept(ModBlocks.POWERED_RAIL_CLASS_5A);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("ElliesRailImprovements starting");
    }
}
