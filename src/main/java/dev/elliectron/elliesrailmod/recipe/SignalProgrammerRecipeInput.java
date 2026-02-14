package dev.elliectron.elliesrailmod.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * RecipeInput for the Signal Programmer.
 * 9 slots in a 3x3 grid, mapped as:
 *   slot 0-2 = top row (left to right)
 *   slot 3-5 = middle row
 *   slot 6-8 = bottom row
 */
public record SignalProgrammerRecipeInput(
        ItemStack slot0, ItemStack slot1, ItemStack slot2,
        ItemStack slot3, ItemStack slot4, ItemStack slot5,
        ItemStack slot6, ItemStack slot7, ItemStack slot8, ItemStack slot9
) implements RecipeInput {

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case 0 -> slot0;
            case 1 -> slot1;
            case 2 -> slot2;
            case 3 -> slot3;
            case 4 -> slot4;
            case 5 -> slot5;
            case 6 -> slot6;
            case 7 -> slot7;
            case 8 -> slot8;
            case 9 -> slot9;
            default -> throw new IllegalArgumentException("No item for index " + slot);
        };
    }

    @Override
    public int size() {
        return 10;
    }
}