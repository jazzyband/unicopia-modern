package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.mixin.StonecutterScreenHandlerRecipesSetter;
import com.minelittlepony.unicopia.recipe.URecipes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class ShapingBenchScreenHandler extends StonecutterMenu {

    private final ContainerLevelAccess context;
    private final Level world;

    private ItemStack inputStack = ItemStack.EMPTY;

    public ShapingBenchScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(syncId, playerInventory, context);
        this.context = context;
        this.world = playerInventory.player.level();
    }

    public ShapingBenchScreenHandler(int syncId, Inventory playerInventory) {
        super(syncId, playerInventory);
        this.context = ContainerLevelAccess.NULL;
        this.world = playerInventory.player.level();
    }

    @Override
    public MenuType<?> getType() {
        return UScreenHandlers.SHAPING_BENCH;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(context, player, UBlocks.SHAPING_BENCH);
    }

    @Override
    public void slotsChanged(Container inventory) {
        ItemStack stack = slots.get(0).getItem();
        if (!stack.is(inputStack.getItem())) {
            inputStack = stack.copy();
            ((StonecutterScreenHandlerRecipesSetter)this).setRecipesForInput(SelectableRecipe.SingleInputSet.empty());
            setData(0, -1);
            slots.get(1).set(ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                ((StonecutterScreenHandlerRecipesSetter)this).setRecipesForInput(InteractionManager.getInstance().getCloudShapingRecipes().selectByInput(container.getItem(0)));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot srcSlot = slots.get(slot);
        if (srcSlot != null && srcSlot.hasItem()) {
            ItemStack movingStack = srcSlot.getItem();
            Item item = movingStack.getItem();
            originalStack = movingStack.copy();
            if (slot == 1) {
                item.onCraftedBy(movingStack, player);
                if (!moveItemStackTo(movingStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                srcSlot.onQuickCraft(movingStack, originalStack);
            } else if (slot == 0
                    ? !moveItemStackTo(movingStack, 2, 38, false)
                    : (((ServerLevel)world).recipeAccess().getRecipeFor(URecipes.CLOUD_SHAPING, new SingleRecipeInput(movingStack), world).isPresent()
                            ? !moveItemStackTo(movingStack, 0, 1, false)
                            : (slot >= 2 && slot < 29
                                ? !moveItemStackTo(movingStack, 29, 38, false)
                                : slot >= 29 && slot < 38 && !moveItemStackTo(movingStack, 2, 29, false)))) {
                return ItemStack.EMPTY;
            }
            if (movingStack.isEmpty()) {
                srcSlot.setByPlayer(ItemStack.EMPTY);
            }
            srcSlot.setChanged();
            if (movingStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }
            srcSlot.onTake(player, movingStack);
            this.broadcastChanges();
        }
        return originalStack;
    }
}
