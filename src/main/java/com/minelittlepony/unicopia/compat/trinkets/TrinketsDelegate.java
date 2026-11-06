package com.minelittlepony.unicopia.compat.trinkets;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface TrinketsDelegate {
    SlotKey MAIN_GLOVE = SlotKey.of("hand", "glove");
    SlotKey SECONDARY_GLOVE = SlotKey.of("offhand", "glove");
    SlotKey NECKLACE = SlotKey.of("chest", "necklace");
    SlotKey FACE = SlotKey.of("head", "face");

    Set<SlotKey> ALL = new TreeSet<>(List.of(MAIN_GLOVE, SECONDARY_GLOVE, NECKLACE, FACE));

    TrinketsDelegate EMPTY = new TrinketsDelegate() {};

    static TrinketsDelegate getInstance(@Nullable LivingEntity entity) {
        return EMPTY;
    }

    static boolean hasTrinkets() {
        return false;
    }

    default void bootstrap() {

    }

    default boolean equipStack(LivingEntity entity, ItemStack stack) {
        EquipmentSlot eq = entity.getEquipmentSlotForItem(stack);
        if (!entity.getItemBySlot(eq).isEmpty()) {
            return false;
        }

        entity.setItemSlot(eq, stack.split(1));
        if (entity instanceof Mob mob) {
            mob.setDropChance(eq, 2.0f);
            mob.setPersistenceRequired();
        }
        return true;
    }

    default Stream<EquippedStack> getEquipped(LivingEntity entity, SlotKey slot, TagKey<Item> tag) {
        return getEquipped(entity, slot, stack -> stack.is(tag));
    }

    default Stream<EquippedStack> getEquipped(LivingEntity entity, SlotKey slot) {
        return getEquipped(entity, slot, (Predicate<ItemStack>)null);
    }

    default Stream<EquippedStack> getEquipped(LivingEntity entity, SlotKey slot, @Nullable Predicate<ItemStack> predicate) {

        if (slot == FACE && (predicate == null || predicate.test(entity.getItemBySlot(EquipmentSlot.HEAD)))) {
            return Stream.of(new EquippedStack(entity, EquipmentSlot.HEAD));
        }
        if ((slot == NECKLACE || slot == MAIN_GLOVE) && (predicate == null || predicate.test(entity.getItemBySlot(EquipmentSlot.CHEST)))) {
            return Stream.of(new EquippedStack(entity, EquipmentSlot.CHEST));
        }
        if (slot == SECONDARY_GLOVE && (predicate == null || predicate.test(entity.getItemBySlot(EquipmentSlot.OFFHAND)))) {
            return Stream.of(new EquippedStack(entity, EquipmentSlot.OFFHAND));
        }

        return Stream.empty();
    }

    default void registerTrinket(Item item) {

    }

    default Optional<Slot> createSlot(SpellbookScreenHandler handler, LivingEntity entity, SlotKey slotId, int i, int x, int y) {
        return Optional.empty();
    }

    default boolean isTrinketSlot(Slot slot) {
        return false;
    }

    record EquippedStack(ItemStack stack, Runnable sendUpdate, Consumer<ItemStack> updater, Consumer<ItemStack> breakStatusSender) {
        public static final EquippedStack EMPTY = new EquippedStack(ItemStack.EMPTY, () -> {}, s -> {}, l -> {});

        EquippedStack(LivingEntity entity, EquipmentSlot slot) {
            this(entity.getItemBySlot(slot), () -> {}, s -> entity.setItemSlot(slot, s), item -> entity.onEquippedItemBroken(item, slot));
        }

        public boolean isEmpty() {
            return stack.isEmpty();
        }

        public void markChanged() {
            sendUpdate.run();
        }

        public void swap(ItemStack newStack) {
            updater.accept(newStack);
        }
    }
}
