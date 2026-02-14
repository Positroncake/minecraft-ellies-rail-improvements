package dev.elliectron.elliesrailmod.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, "elliesrailmod");

    public static final Supplier<MenuType<RailWorkshopMenu>> RAIL_WORKSHOP =
            MENUS.register("rail_workshop",
                    () -> new MenuType<>(RailWorkshopMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<SignalProgrammerMenu>> SIGNAL_PROGRAMMER =
            MENUS.register("signal_programmer",
                    () -> new MenuType<>(SignalProgrammerMenu::new, FeatureFlags.DEFAULT_FLAGS));
}