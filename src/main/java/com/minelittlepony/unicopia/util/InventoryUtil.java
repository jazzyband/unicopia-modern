package com.minelittlepony.unicopia.util;

import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public interface InventoryUtil {
    static Stream<ItemStack> stream(Container inventory) {
        return slots(inventory).map(inventory::getItem);
    }

    static Stream<ItemStack> stream(RecipeInput inventory) {
        return slots(inventory).map(inventory::getItem);
    }

    static Stream<Integer> slots(Container inventory) {
        return Stream.iterate(0, i -> i < inventory.getContainerSize(), i -> i + 1);
    }

    static Stream<Integer> slots(RecipeInput inventory) {
        return Stream.iterate(0, i -> i < inventory.size(), i -> i + 1);
    }

    static int getOpenSlot(Container inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    static boolean contentEquals(Container a, Container b) {
        if (a.getContainerSize() != b.getContainerSize()) {
            return false;
        }
        for (int i = 0; i < a.getContainerSize(); i++) {
            if (!ItemStack.matches(a.getItem(i), b.getItem(i))) {
                return false;
            }
        }
        return true;
    }

    static <I extends Container> I copy(Container from, Function<Integer, I> factory) {
        return copyInto(from, factory.apply(from.getContainerSize()));
    }

    static <I extends Container> I copyInto(Container from, I into) {
        for (int i = 0; i < from.getContainerSize(); i++) {
            into.setItem(i, from.getItem(i).copy());
        }
        return into;
    }
}
