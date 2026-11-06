package com.minelittlepony.unicopia.block;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.storage.loot.LootTable;

public interface StrippingLootable {
    boolean onStripped(UseOnContext context);

    Optional<ResourceKey<LootTable>> getStrippingLootTableKey();
}
