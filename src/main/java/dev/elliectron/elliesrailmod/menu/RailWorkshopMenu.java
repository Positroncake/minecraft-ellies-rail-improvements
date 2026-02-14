package dev.elliectron.elliesrailmod.menu;

import dev.elliectron.elliesrailmod.block.ModBlocks;
import dev.elliectron.elliesrailmod.block.entity.RailWorkshopBlockEntity;
import dev.elliectron.elliesrailmod.recipe.RailWorkshopRecipe;
import dev.elliectron.elliesrailmod.recipe.RailWorkshopRecipeInput;
import dev.elliectron.elliesrailmod.recipe.ModRecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.Optional;

/**
 * Menu for the Rail Workshop.
 *   4 input slots + 1 output/preview slot.
 *   Taking the output consumes one of each non-empty input.
 *
 * Slot indices:
 *   0 = input1 (optional), 1 = input2 (mandatory), 2 = input3 (optional), 3 = input4 (optional)
 *   4 = result
 *   5–31 = player inventory, 32–40 = player hotbar
 */
public class RailWorkshopMenu extends AbstractContainerMenu {

    public static final int INPUT1_SLOT = 0;
    public static final int INPUT2_SLOT = 1;
    public static final int INPUT3_SLOT = 2;
    public static final int INPUT4_SLOT = 3;
    public static final int RESULT_SLOT = 4;
    private static final int PLAYER_INV_START = 5;
    private static final int PLAYER_INV_END = 32;
    private static final int HOTBAR_END = 41;

    private final IItemHandler inputHandler;
    private final ItemStackHandler resultHandler = new ItemStackHandler(1);
    private final ContainerLevelAccess access;

    // --- Client constructor ---
    public RailWorkshopMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, ContainerLevelAccess.NULL);
    }

    // --- Server constructor ---
    public RailWorkshopMenu(int containerId, Inventory playerInventory,
                            RailWorkshopBlockEntity blockEntity,
                            ContainerLevelAccess access) {
        super(ModMenuTypes.RAIL_WORKSHOP.get(), containerId);
        this.access = access;

        if (blockEntity != null) {
            this.inputHandler = blockEntity.getInputHandler();
        } else {
            this.inputHandler = new ItemStackHandler(RailWorkshopBlockEntity.INPUT_SLOTS);
        }

        // Input slots — adjust x,y to match your GUI texture
        this.addSlot(new InputSlot(inputHandler, 0, 21, 29));   // Input 1 (optional)
        this.addSlot(new InputSlot(inputHandler, 1, 48, 29));   // Input 2 (mandatory)
        this.addSlot(new InputSlot(inputHandler, 2, 75, 29));   // Input 3 (optional)
        this.addSlot(new InputSlot(inputHandler, 3, 58, 53));   // Input 4 (optional)

        // Result slot
        this.addSlot(new ResultSlot(resultHandler, 0, 137, 33));

        // Player inventory (3 rows × 9 cols)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18));
            }
        }

        // Player hotbar (1 row × 9 cols)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        updateResult();
    }

    // ========== Recipe matching ==========

    public void updateResult() {
        resultHandler.setStackInSlot(0, ItemStack.EMPTY);

        access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel) {
                RailWorkshopRecipeInput recipeInput = new RailWorkshopRecipeInput(
                        inputHandler.getStackInSlot(0),
                        inputHandler.getStackInSlot(1),
                        inputHandler.getStackInSlot(2),
                        inputHandler.getStackInSlot(3)
                );

                Optional<RecipeHolder<RailWorkshopRecipe>> match =
                        serverLevel.getRecipeManager()
                                .getRecipeFor(ModRecipes.RAIL_WORKSHOP_TYPE.get(),
                                        recipeInput, serverLevel);

                match.ifPresent(holder -> {
                    ItemStack result = holder.value()
                            .assemble(recipeInput, serverLevel.registryAccess());
                    resultHandler.setStackInSlot(0, result);
                });
            }
        });

        broadcastChanges();
    }

    // ========== Custom slot classes ==========

    private class InputSlot extends SlotItemHandler {
        public InputSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            updateResult();
        }
    }

    private class ResultSlot extends SlotItemHandler {
        public ResultSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            for (int i = 0; i < RailWorkshopBlockEntity.INPUT_SLOTS; i++) {
                if (!inputHandler.getStackInSlot(i).isEmpty()) {
                    inputHandler.extractItem(i, 1, false);
                }
            }
            updateResult();
            super.onTake(player, stack);
        }
    }

    // ========== Standard overrides ==========

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RAIL_WORKSHOP.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();
        ItemStack originalStack = slotStack.copy();

        if (slotIndex == RESULT_SLOT) {
            if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(slotStack, originalStack);
        } else if (slotIndex >= PLAYER_INV_START) {
            if (!this.moveItemStackTo(slotStack, INPUT1_SLOT, INPUT4_SLOT + 1, false)) {
                if (slotIndex < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(slotStack, PLAYER_INV_END, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return originalStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }
}