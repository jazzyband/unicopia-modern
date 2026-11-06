package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class SpellbookInventory extends TransientCraftingContainer {
    private final SpellbookScreenHandler handler;

    public SpellbookInventory(SpellbookScreenHandler handler, int width, int height) {
        super(handler, width, height);
        this.handler = handler;
    }

    public ItemStack getItemToModify() {
        return handler.gemSlot.getItem();
    }

    public float getFactor(int slot) {
        Slot s = handler.slots.get(slot);
        return s instanceof SpellbookSlot ? ((SpellbookSlot)s).getWeight() : 0;
    }

    public SpellTraits getTraits() {
        return SpellTraits.union(InventoryUtil.slots(this)
                .filter(slot -> slot != handler.GEM_SLOT_INDEX)
                .map(slot -> SpellTraits.of(getItem(slot)).multiply(getFactor(slot)))
                .toArray(SpellTraits[]::new)
        );
    }

    public SpellbookRecipe.Input createInput() {
        return new SpellbookRecipe.Input(getItemToModify(), InventoryUtil.slots(this).map(slot -> {
            return Pair.of(getFactor(slot), getItem(slot));
        }).toList(), getTraits(), handler.GEM_SLOT_INDEX);
    }
}