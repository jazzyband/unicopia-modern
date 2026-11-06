package com.minelittlepony.unicopia.util;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public interface Dispensable {
    default DispenseItemBehavior createDispenserBehaviour() {
        return new DefaultDispenseItemBehavior() {
            private InteractionResult result;
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                result = dispenseStack(source, stack);

                if (!result.consumesAction()) {
                    return super.execute(source, stack);
                }

                return result instanceof InteractionResult.Success success ? success.heldItemTransformedTo() : stack.split(1);
            }

            @Override
            protected void playSound(BlockSource pointer) {
                if (!result.consumesAction()) {
                    super.playSound(pointer);
                }
            }

            @Override
            protected void playAnimation(BlockSource pointer, Direction side) {
                if (!result.consumesAction()) {
                    super.playAnimation(pointer, side);
                }
            }
        };
    }

    /**
     * Called to dispense this stack.
     */
    InteractionResult dispenseStack(BlockSource source, ItemStack stack);
}
