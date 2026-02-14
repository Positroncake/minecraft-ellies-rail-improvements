package dev.elliectron.elliesrailmod.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, "elliesrailmod");

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, "elliesrailmod");

    public static final Supplier<RecipeType<RailWorkshopRecipe>> RAIL_WORKSHOP_TYPE =
            RECIPE_TYPES.register("rail_workshop",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(
                            "elliesrailmod", "rail_workshop")));

    public static final Supplier<RecipeSerializer<RailWorkshopRecipe>> RAIL_WORKSHOP_SERIALIZER =
            RECIPE_SERIALIZERS.register("rail_workshop",
                    RailWorkshopRecipe.Serializer::new);

    public static final Supplier<RecipeType<SignalProgrammerRecipe>> SIGNAL_PROGRAMMER_TYPE =
            RECIPE_TYPES.register("signal_programmer",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(
                            "elliesrailmod", "signal_programmer")));

    public static final Supplier<RecipeSerializer<SignalProgrammerRecipe>> SIGNAL_PROGRAMMER_SERIALIZER =
            RECIPE_SERIALIZERS.register("signal_programmer",
                    SignalProgrammerRecipe.Serializer::new);
}