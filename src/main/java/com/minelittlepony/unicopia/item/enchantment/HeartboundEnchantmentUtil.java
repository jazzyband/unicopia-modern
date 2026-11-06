package com.minelittlepony.unicopia.item.enchantment;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import com.minelittlepony.unicopia.util.InventoryUtil;

public interface HeartboundEnchantmentUtil {
    static InventorySnapshot createSnapshot(Container inventory) {
        return inventory.isEmpty() ? InventorySnapshot.EMPTY : new InventorySnapshot(InventoryUtil.stream(inventory).toList());
    }

    public record InventorySnapshot(List<ItemStack> stacks, boolean empty) {
        public static final InventorySnapshot EMPTY = new InventorySnapshot(List.of(), true);

        public InventorySnapshot(List<ItemStack> stacks) {
            this(stacks, stacks.stream().anyMatch(i -> !i.isEmpty()));
        }

        public void restoreInto(Container inventory) {
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                inventory.setItem(i, i >= stacks.size() ? ItemStack.EMPTY : stacks.get(i));
            }
        }
    }
}
