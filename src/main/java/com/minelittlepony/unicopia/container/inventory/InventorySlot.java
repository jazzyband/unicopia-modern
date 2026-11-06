package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.container.*;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class InventorySlot extends Slot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;

    public InventorySlot(SpellbookScreenHandler handler, Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.handler = handler;
    }

    @Override
    public boolean isActive() {
       return handler.canShowSlots(SlotType.INVENTORY);
    }

    @Override
    public boolean showTraits() {
        return false;
    }

    @Override
    public float getBackSpriteOpacity() {
        return 1F;
    }
}