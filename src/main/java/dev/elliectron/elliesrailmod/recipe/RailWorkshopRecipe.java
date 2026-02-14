package dev.elliectron.elliesrailmod.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Rail Workshop recipe. Order-sensitive, position-based matching.
 *   input1: Optional (slot 0)
 *   input2: Mandatory (slot 1) — every recipe requires this
 *   input3: Optional (slot 2)
 *   input4: Optional (slot 3)
 *
 * For each optional input:
 *   - If present in the recipe, the corresponding slot must contain a matching item.
 *   - If absent from the recipe, the corresponding slot must be empty.
 */
public class RailWorkshopRecipe implements Recipe<RailWorkshopRecipeInput> {

    private final Optional<Ingredient> input1;
    private final Ingredient input2;
    private final Optional<Ingredient> input3;
    private final Optional<Ingredient> input4;
    private final ItemStack result;

    public RailWorkshopRecipe(Optional<Ingredient> input1, Ingredient input2,
                              Optional<Ingredient> input3, Optional<Ingredient> input4,
                              ItemStack result) {
        this.input1 = input1;
        this.input2 = input2;
        this.input3 = input3;
        this.input4 = input4;
        this.result = result;
    }

    public Optional<Ingredient> getInput1() { return input1; }
    public Ingredient getInput2() { return input2; }
    public Optional<Ingredient> getInput3() { return input3; }
    public Optional<Ingredient> getInput4() { return input4; }
    public ItemStack getResult() { return result; }

    @Override
    public boolean matches(RailWorkshopRecipeInput input, Level level) {
        // Slot 1 (input2) is always mandatory
        if (!input2.test(input.slot1())) {
            return false;
        }

        // Slot 0 (input1) — optional
        if (input1.isPresent()) {
            if (!input1.get().test(input.slot0())) return false;
        } else {
            if (!input.slot0().isEmpty()) return false;
        }

        // Slot 2 (input3) — optional
        if (input3.isPresent()) {
            if (!input3.get().test(input.slot2())) return false;
        } else {
            if (!input.slot2().isEmpty()) return false;
        }

        // Slot 3 (input4) — optional
        if (input4.isPresent()) {
            if (!input4.get().test(input.slot3())) return false;
        } else {
            if (!input.slot3().isEmpty()) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(RailWorkshopRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RAIL_WORKSHOP_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.RAIL_WORKSHOP_SERIALIZER.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    // ========== Serializer ==========

    public static class Serializer implements RecipeSerializer<RailWorkshopRecipe> {

        public static final MapCodec<RailWorkshopRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Ingredient.CODEC.optionalFieldOf("input1")
                                .forGetter(RailWorkshopRecipe::getInput1),
                        Ingredient.CODEC.fieldOf("input2")
                                .forGetter(RailWorkshopRecipe::getInput2),
                        Ingredient.CODEC.optionalFieldOf("input3")
                                .forGetter(RailWorkshopRecipe::getInput3),
                        Ingredient.CODEC.optionalFieldOf("input4")
                                .forGetter(RailWorkshopRecipe::getInput4),
                        ItemStack.STRICT_CODEC.fieldOf("result")
                                .forGetter(RailWorkshopRecipe::getResult)
                ).apply(inst, RailWorkshopRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RailWorkshopRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC),
                        RailWorkshopRecipe::getInput1,
                        Ingredient.CONTENTS_STREAM_CODEC,
                        RailWorkshopRecipe::getInput2,
                        ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC),
                        RailWorkshopRecipe::getInput3,
                        ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC),
                        RailWorkshopRecipe::getInput4,
                        ItemStack.STREAM_CODEC,
                        RailWorkshopRecipe::getResult,
                        RailWorkshopRecipe::new
                );

        @Override
        public MapCodec<RailWorkshopRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RailWorkshopRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}