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
 * Signal Programmer recipe. All 10 inputs are optional, position-sensitive.
 *
 * If an input is present in the recipe, the slot must contain a matching item.
 * If an input is absent, the slot must be empty.
 */
public class SignalProgrammerRecipe implements Recipe<SignalProgrammerRecipeInput> {

    private final Optional<Ingredient> input1;
    private final Optional<Ingredient> input2;
    private final Optional<Ingredient> input3;
    private final Optional<Ingredient> input4;
    private final Optional<Ingredient> input5;
    private final Optional<Ingredient> input6;
    private final Optional<Ingredient> input7;
    private final Optional<Ingredient> input8;
    private final Optional<Ingredient> input9;
    private final Optional<Ingredient> input10;
    private final ItemStack result;

    public SignalProgrammerRecipe(
            Optional<Ingredient> input1, Optional<Ingredient> input2, Optional<Ingredient> input3,
            Optional<Ingredient> input4, Optional<Ingredient> input5, Optional<Ingredient> input6,
            Optional<Ingredient> input7, Optional<Ingredient> input8, Optional<Ingredient> input9,
            Optional<Ingredient> input10, ItemStack result) {
        this.input1 = input1;
        this.input2 = input2;
        this.input3 = input3;
        this.input4 = input4;
        this.input5 = input5;
        this.input6 = input6;
        this.input7 = input7;
        this.input8 = input8;
        this.input9 = input9;
        this.input10 = input10;
        this.result = result;
    }

    public Optional<Ingredient> getInput1() { return input1; }
    public Optional<Ingredient> getInput2() { return input2; }
    public Optional<Ingredient> getInput3() { return input3; }
    public Optional<Ingredient> getInput4() { return input4; }
    public Optional<Ingredient> getInput5() { return input5; }
    public Optional<Ingredient> getInput6() { return input6; }
    public Optional<Ingredient> getInput7() { return input7; }
    public Optional<Ingredient> getInput8() { return input8; }
    public Optional<Ingredient> getInput9() { return input9; }
    public Optional<Ingredient> getInput10() { return input10; }
    public ItemStack getResult() { return result; }

    private boolean testSlot(Optional<Ingredient> recipeInput, ItemStack slotStack) {
        if (recipeInput.isPresent()) {
            return recipeInput.get().test(slotStack);
        } else {
            return slotStack.isEmpty();
        }
    }

    @Override
    public boolean matches(SignalProgrammerRecipeInput input, Level level) {
        return testSlot(input1, input.slot0())
                && testSlot(input2, input.slot1())
                && testSlot(input3, input.slot2())
                && testSlot(input4, input.slot3())
                && testSlot(input5, input.slot4())
                && testSlot(input6, input.slot5())
                && testSlot(input7, input.slot6())
                && testSlot(input8, input.slot7())
                && testSlot(input9, input.slot8())
                && testSlot(input10, input.slot9());
    }

    @Override
    public ItemStack assemble(SignalProgrammerRecipeInput input, HolderLookup.Provider registries) {
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
        return ModRecipes.SIGNAL_PROGRAMMER_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SIGNAL_PROGRAMMER_SERIALIZER.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    // ========== Serializer ==========

    public static class Serializer implements RecipeSerializer<SignalProgrammerRecipe> {

        // RecordCodecBuilder.mapCodec only supports up to 16 fields, so we're fine with 11.
        public static final MapCodec<SignalProgrammerRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Ingredient.CODEC.optionalFieldOf("input1").forGetter(SignalProgrammerRecipe::getInput1),
                        Ingredient.CODEC.optionalFieldOf("input2").forGetter(SignalProgrammerRecipe::getInput2),
                        Ingredient.CODEC.optionalFieldOf("input3").forGetter(SignalProgrammerRecipe::getInput3),
                        Ingredient.CODEC.optionalFieldOf("input4").forGetter(SignalProgrammerRecipe::getInput4),
                        Ingredient.CODEC.optionalFieldOf("input5").forGetter(SignalProgrammerRecipe::getInput5),
                        Ingredient.CODEC.optionalFieldOf("input6").forGetter(SignalProgrammerRecipe::getInput6),
                        Ingredient.CODEC.optionalFieldOf("input7").forGetter(SignalProgrammerRecipe::getInput7),
                        Ingredient.CODEC.optionalFieldOf("input8").forGetter(SignalProgrammerRecipe::getInput8),
                        Ingredient.CODEC.optionalFieldOf("input9").forGetter(SignalProgrammerRecipe::getInput9),
                        Ingredient.CODEC.optionalFieldOf("input10").forGetter(SignalProgrammerRecipe::getInput10),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SignalProgrammerRecipe::getResult)
                ).apply(inst, SignalProgrammerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SignalProgrammerRecipe> STREAM_CODEC =
                new StreamCodec<>() {
                    private final StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> OPT_INGREDIENT =
                            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC);

                    @Override
                    public SignalProgrammerRecipe decode(RegistryFriendlyByteBuf buf) {
                        return new SignalProgrammerRecipe(
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                OPT_INGREDIENT.decode(buf),
                                ItemStack.STREAM_CODEC.decode(buf)
                        );
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buf, SignalProgrammerRecipe recipe) {
                        OPT_INGREDIENT.encode(buf, recipe.input1);
                        OPT_INGREDIENT.encode(buf, recipe.input2);
                        OPT_INGREDIENT.encode(buf, recipe.input3);
                        OPT_INGREDIENT.encode(buf, recipe.input4);
                        OPT_INGREDIENT.encode(buf, recipe.input5);
                        OPT_INGREDIENT.encode(buf, recipe.input6);
                        OPT_INGREDIENT.encode(buf, recipe.input7);
                        OPT_INGREDIENT.encode(buf, recipe.input8);
                        OPT_INGREDIENT.encode(buf, recipe.input9);
                        OPT_INGREDIENT.encode(buf, recipe.input10);
                        ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                    }
                };

        @Override
        public MapCodec<SignalProgrammerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SignalProgrammerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}