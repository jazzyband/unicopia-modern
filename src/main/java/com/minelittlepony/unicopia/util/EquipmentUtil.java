package com.minelittlepony.unicopia.util;

import java.util.stream.Stream;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;

public interface EquipmentUtil {

    static Stream<ItemStack> getInventory(LivingEntity entity) {
        if (entity instanceof Player player) {
            return InventoryUtil.stream(player.getInventory());
        }

        if (entity instanceof InventoryCarrier owner) {
            return InventoryUtil.stream(owner.getInventory());
        }

        return EquipmentSlot.VALUES.stream().filter(entity::hasItemInSlot).map(entity::getItemBySlot);
    }


    static Stream<ItemStack> getArmor(LivingEntity entity) {
        Stream<ItemStack> baseArmorStacks = EquipmentSlot.VALUES.stream()
                .filter(EquipmentSlot::isArmor)
                .filter(entity::hasItemInSlot)
                .map(entity::getItemBySlot);
        return TrinketsDelegate.hasTrinkets() ? Stream.concat(
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.NECKLACE).map(TrinketsDelegate.EquippedStack::stack),
                baseArmorStacks
        ) : baseArmorStacks;
    }
}
