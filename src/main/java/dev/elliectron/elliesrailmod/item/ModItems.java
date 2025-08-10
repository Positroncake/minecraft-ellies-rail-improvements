package dev.elliectron.elliesrailmod.item;

import dev.elliectron.elliesrailmod.ElliesRailImprovements;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ElliesRailImprovements.MODID);

    public static final DeferredItem<Item> SIGNAL_PROCEED = ITEMS.register("signal_proceed", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SIGNAL_OVERRIDE = ITEMS.register("signal_override", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SIGNAL_SWITCH_ALTERNATE = ITEMS.register("signal_switch_alternate", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SIGNAL_STOP = ITEMS.register("signal_stop", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SIGNAL_E_STOP = ITEMS.register("signal_e_stop", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SIGNAL_SPEED_LIMITED = ITEMS.register("signal_speed_limited", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SIGNAL_SPEED_MEDIUM = ITEMS.register("signal_speed_medium", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SIGNAL_SPEED_DIVERGING = ITEMS.register("signal_speed_diverging", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SIGNAL_SPEED_RESTRICTED = ITEMS.register("signal_speed_restricted", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> RAIL_SEGMENT = ITEMS.register("rail_segment", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> PREMIUM_RAIL_SEGMENT = ITEMS.register("premium_rail_segment", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> POWERED_RAIL_SEGMENT = ITEMS.register("powered_rail_segment", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> POWERED_PREMIUM_RAIL_SEGMENT = ITEMS.register("powered_premium_rail_segment", () -> new Item(new Item.Properties().stacksTo(64)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}