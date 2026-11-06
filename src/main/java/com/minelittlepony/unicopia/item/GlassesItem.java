package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterials;

public class GlassesItem extends WearableItem {
    public GlassesItem(Item.Properties settings) {
        super(settings, ArmorMaterials.LEATHER.equipSound());
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    public static boolean isCoolAndHasShades(LivingEntity entity) {
        ItemStack glasses = getForEntity(entity).stack();
        Component customName = glasses.get(DataComponents.CUSTOM_NAME);
        return glasses.is(UTags.Items.TINTED_SHADES) && customName != null && "Cool Shades".equals(customName.getString());
    }

    public static TrinketsDelegate.EquippedStack getForEntity(LivingEntity entity) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE, stack -> stack.getItem() instanceof GlassesItem)
                .findFirst()
                .orElse(TrinketsDelegate.EquippedStack.EMPTY);
    }
}
