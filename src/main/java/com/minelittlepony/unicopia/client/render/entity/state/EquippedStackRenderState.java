package com.minelittlepony.unicopia.client.render.entity.state;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class EquippedStackRenderState {
    public boolean empty;
    @SuppressWarnings("deprecation")
    public ResourceKey<Item> key = Items.AIR.builtInRegistryHolder().key();

    public void update(TrinketsDelegate.EquippedStack stack) {
        empty = stack.isEmpty();
        key = stack.stack().getItem().builtInRegistryHolder().unwrapKey().orElseThrow();
    }
}
