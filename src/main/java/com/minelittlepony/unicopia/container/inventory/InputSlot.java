package com.minelittlepony.unicopia.container.inventory;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.container.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class InputSlot extends Slot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;
    private final float weight;

    public InputSlot(SpellbookScreenHandler handler, Container inventory, int index, HexagonalCraftingGrid.Slot params) {
        super(inventory, index, params.left(), params.top());
        this.handler = handler;
        this.weight = params.weight();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public boolean isActive() {
       return handler.canShowSlots(SlotType.CRAFTING) && !handler.outputSlot.isActive();
    }

    @Override
    @Nullable
    public Identifier getForegroundIdentifier() {
        return GEM;
    }
}