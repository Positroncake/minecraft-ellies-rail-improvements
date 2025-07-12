package dev.elliectron.ellierailmod.item;

import dev.elliectron.ellierailmod.ElliesRailImprovements;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ElliesRailImprovements.MODID);
    public static final DeferredItem<Item> SIGNAL_SWITCH_ALTERNATE = ITEMS.register("signal_switch_alternate", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SIGNAL_SWITCH_OVERRIDE = ITEMS.register("signal_switch_override", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
