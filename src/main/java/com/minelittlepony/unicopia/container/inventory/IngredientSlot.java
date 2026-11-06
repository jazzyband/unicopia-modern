package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.container.*;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class IngredientSlot extends Slot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;
    private final float weight;

    public IngredientSlot(SpellbookScreenHandler handler, Container inventory, int index, HexagonalCraftingGrid.Slot params) {
        super(inventory, index, params.left(), params.top());
        this.handler = handler;
        weight = params.weight();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public boolean isActive() {
       return handler.canShowSlots(SlotType.CRAFTING) && super.isActive();
    }
}