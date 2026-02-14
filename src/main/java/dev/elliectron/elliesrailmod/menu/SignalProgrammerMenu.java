package dev.elliectron.elliesrailmod.menu;

import dev.elliectron.elliesrailmod.block.ModBlocks;
import dev.elliectron.elliesrailmod.block.entity.SignalProgrammerBlockEntity;
import dev.elliectron.elliesrailmod.recipe.SignalProgrammerRecipe;
import dev.elliectron.elliesrailmod.recipe.SignalProgrammerRecipeInput;
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
 * Menu for the Signal Programmer.
 *   10 input slots (all optional) + 1 output/preview slot.
 *
 * Slot indices:
 *   0-9 = inputs
 *   10 = result
 *   11-37 = player inventory, 38-46 = player hotbar
 */
public class SignalProgrammerMenu extends AbstractContainerMenu {

    public static final int INPUT_START = 0;
    public static final int INPUT_END = 9;
    public static final int RESULT_SLOT = 10;
    private static final int PLAYER_INV_START = 11;
    private static final int PLAYER_INV_END = 38;
    private static final int HOTBAR_END = 47;

    private final IItemHandler inputHandler;
    private final ItemStackHandler resultHandler = new ItemStackHandler(1);
    private final ContainerLevelAccess access;

    // --- Client constructor ---
    public SignalProgrammerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, ContainerLevelAccess.NULL);
    }

    // --- Server constructor ---
    public SignalProgrammerMenu(int containerId, Inventory playerInventory,
                                SignalProgrammerBlockEntity blockEntity,
                                ContainerLevelAccess access) {
        super(ModMenuTypes.SIGNAL_PROGRAMMER.get(), containerId);
        this.access = access;

        if (blockEntity != null) {
            this.inputHandler = blockEntity.getInputHandler();
        } else {
            this.inputHandler = new ItemStackHandler(SignalProgrammerBlockEntity.INPUT_SLOTS);
        }

        // Input slots — adjust x,y to match your GUI texture
        this.addSlot(new InputSlot(inputHandler, 0, 78, 34));
        this.addSlot(new InputSlot(inputHandler, 1, 126, 10));
        this.addSlot(new InputSlot(inputHandler, 2, 150, 10));
        this.addSlot(new InputSlot(inputHandler, 3, 102, 34));
        this.addSlot(new InputSlot(inputHandler, 4, 126, 34));
        this.addSlot(new InputSlot(inputHandler, 5, 150, 34));
        this.addSlot(new InputSlot(inputHandler, 6, 78, 58));
        this.addSlot(new InputSlot(inputHandler, 7, 102, 58));
        this.addSlot(new InputSlot(inputHandler, 8, 126, 58));
        this.addSlot(new InputSlot(inputHandler, 9, 150, 58));

        // Result slot
        this.addSlot(new ResultSlot(resultHandler, 0, 10, 44));

        // Player inventory (3 rows × 9 cols)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18));
            }
        }

        // Player hotbar
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
                SignalProgrammerRecipeInput recipeInput = new SignalProgrammerRecipeInput(
                        inputHandler.getStackInSlot(0),
                        inputHandler.getStackInSlot(1),
                        inputHandler.getStackInSlot(2),
                        inputHandler.getStackInSlot(3),
                        inputHandler.getStackInSlot(4),
                        inputHandler.getStackInSlot(5),
                        inputHandler.getStackInSlot(6),
                        inputHandler.getStackInSlot(7),
                        inputHandler.getStackInSlot(8),
                        inputHandler.getStackInSlot(9)
                );

                Optional<RecipeHolder<SignalProgrammerRecipe>> match =
                        serverLevel.getRecipeManager()
                                .getRecipeFor(ModRecipes.SIGNAL_PROGRAMMER_TYPE.get(),
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
            for (int i = 0; i < SignalProgrammerBlockEntity.INPUT_SLOTS; i++) {
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
        return stillValid(access, player, ModBlocks.SIGNAL_PROGRAMMER.get());
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
            if (!this.moveItemStackTo(slotStack, INPUT_START, INPUT_END + 1, false)) {
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