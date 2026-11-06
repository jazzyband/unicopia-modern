package com.minelittlepony.unicopia.entity.behaviour;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Inventory (
        Map<EquipmentSlot, ItemStack> equipment,
        Optional<NonNullList<ItemStack>> mainInventory
    ) {
    public static final Codec<Inventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(EquipmentSlot.CODEC, ItemStack.CODEC).fieldOf("equipment").forGetter(Inventory::equipment),
            CodecUtils.defaultedList(ItemStack.CODEC, ItemStack.EMPTY).optionalFieldOf("main").forGetter(Inventory::mainInventory)
    ).apply(instance, Inventory::new));

    public static Optional<Inventory> of(LivingEntity entity) {
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (var slot : EquipmentSlot.values()) {
            equipment.put(slot, entity.getItemBySlot(slot));
        }

        if (entity instanceof Player player) {
            net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
            NonNullList<ItemStack> mainInventory = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
            for (int i = 0; i < mainInventory.size(); i++) {
                mainInventory.set(i, inventory.getItem(i));
            }

            return Optional.of(new Inventory(equipment, Optional.of(mainInventory)));
        }

        return Optional.of(new Inventory(equipment, Optional.empty()));
    }

    /**
     * Copies the inventory into another entity.
     *
     * @return Returns the left overs that could not be copied
     */
    public Inventory copyInto(ServerLevel world, LivingEntity into) {
        if (into instanceof Player player) {
            mainInventory().ifPresentOrElse(main -> {
                net.minecraft.world.entity.player.Inventory pe = player.getInventory();
                int i = 0;
                for (; i < pe.getContainerSize(); i++) {
                    pe.setItem(i, i < main.size() ? main.get(i) : ItemStack.EMPTY);
                }
                for (; i < main.size(); i++) {
                    into.spawnAtLocation(world, main.get(i));
                }
            }, () -> {
                net.minecraft.world.entity.player.Inventory pe = player.getInventory();
                for (int i = 0; i < pe.getContainerSize(); i++) {
                    pe.setItem(i, ItemStack.EMPTY);
                }
                equipment().forEach(player::setItemSlot);
            });

            return null;
        }

        equipment().forEach(into::setItemSlot);
        return this;
    }

    public static void swapInventories(ServerLevel world, LivingEntity me, Optional<Inventory> myInv, LivingEntity them, Optional<Inventory> theirInv,
            Consumer<Inventory> outOverflowConsumer,
            Consumer<Inventory> inOverflowConsumer) {
        Optional<Inventory> outOverflow = Inventory.copyInventoryInto(world, myInv, them);
        Optional<Inventory> inOverflow = Inventory.copyInventoryInto(world, theirInv, me);

        outOverflow.ifPresent(outOverflowConsumer);
        inOverflow.ifPresent(inOverflowConsumer);
    }

    public static Optional<Inventory> copyInventoryInto(ServerLevel world, Optional<Inventory> inventory, LivingEntity to) {
        return inventory.map(inv -> inv.copyInto(world, to));
    }
}