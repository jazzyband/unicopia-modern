package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.ItemImpl.ClingyItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface ItemDuck extends ItemLike, TickableItem, ClingyItem {
    static ItemDuck of(ItemStack stack) {
        return (ItemDuck)stack.getItem();
    }
}
