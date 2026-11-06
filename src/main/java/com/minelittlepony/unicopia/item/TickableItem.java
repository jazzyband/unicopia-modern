package com.minelittlepony.unicopia.item;

import java.util.List;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import com.minelittlepony.unicopia.entity.IItemEntity;

public interface TickableItem {
    static <T extends Item> T registerTickCallback(T item, GroundTickCallback callback) {
        ((ItemDuck)item).addGroundTickCallback(callback);
        return item;
    }

    default List<GroundTickCallback> getCallbacks() {
        throw new RuntimeException("Implemented by mixin");
    }

    default void addGroundTickCallback(GroundTickCallback callback) {
        getCallbacks().add(callback);
    }

    default InteractionResult onGroundTick(IItemEntity entity) {
        for (var callback : getCallbacks()) {
            InteractionResult result = callback.onGroundTick(entity);
            if (result.consumesAction()) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    default void inFrameTick(ItemFrame entity) {

    }

    public interface GroundTickCallback {
        InteractionResult onGroundTick(IItemEntity entity);
    }
}
