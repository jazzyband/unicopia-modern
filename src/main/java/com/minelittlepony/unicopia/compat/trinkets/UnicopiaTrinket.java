package com.minelittlepony.unicopia.compat.trinkets;

import com.minelittlepony.unicopia.entity.ItemTracker;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.WearableItem;
import com.minelittlepony.unicopia.item.component.Issuer;

import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.core.Accessory;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.gameevent.GameEvent;

public class UnicopiaTrinket implements Accessory {

    private final Item item;

    public UnicopiaTrinket(Item item) {
        this.item = item;
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot) {
        if (slot.entity().isSpectator() || stack.isEmpty()) {
            return;
        }

        if (!(stack.getItem() instanceof ItemTracker.Trackable)) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            Holder<SoundEvent> soundEvent = equippable == null ? null : equippable.equipSound();
            if (soundEvent != null) {
                slot.entity().gameEvent(GameEvent.EQUIP);
                slot.entity().playSound(soundEvent.value(), 1, 1);
            }
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot) {
        if (stack.getItem() instanceof ItemTracker.Trackable t) {
            Living<?> l = Living.living(slot.entity());
            t.onUnequipped(l, l.getArmour().forceRemove(t));
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        Holder<SoundEvent> soundEvent = equippable == null ? null : equippable.equipSound();
        if (soundEvent != null) {
            slot.entity().gameEvent(GameEvent.EQUIP);
            slot.entity().playSound(soundEvent.value(), 1, 1);
        }
    }

    @Override
    public int maxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot) {
        if (item instanceof FriendshipBraceletItem && !Issuer.isSigned(stack)) {
            return false;
        }

        return slot.getStack().isEmpty();
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot) {
        if (slot.entity().level() instanceof ServerLevel sw) {
            item.inventoryTick(stack, sw, slot.entity(), null);
        }
    }

    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference slot, AccessoryAttributeBuilder builder) {
        Accessory.super.getDynamicModifiers(stack, slot, builder);

        if (item instanceof WearableItem wearable) {
            EquipmentSlot es = wearable.getSlotType(stack);
            stack.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().forEach(entry -> {
                if (entry.slot().test(es)) {
                    builder.addStackable(entry.attribute(), entry.modifier());
                }
            });
        }
    }
}
