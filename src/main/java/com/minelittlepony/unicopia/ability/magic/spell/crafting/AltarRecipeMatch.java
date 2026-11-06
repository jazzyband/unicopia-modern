package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.MutableVector;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class AltarRecipeMatch {
    private final Vec3 position;
    private final List<ItemEntity> stacks = new ArrayList<>();
    private final Int2ObjectMap<ItemInput> inputs = new Int2ObjectOpenHashMap<>();
    private final int minCrafts;
    private final ItemStack result;

    public AltarRecipeMatch(Stream<ItemEntity> stacks, ItemStack result) {
        MutableVector position = new MutableVector(Vec3.ZERO);
        stacks.forEach(stack -> {
            this.stacks.add(stack);
            position.add(stack.position());
            inputs.computeIfAbsent(Item.getId(stack.getItem().getItem()), i -> new ItemInput()).add(stack);
        });
        position.multiply(1D/this.stacks.size());
        this.position = position.toImmutable();
        minCrafts = inputs.values().stream().mapToInt(i -> i.count).min().orElse(0);
        this.result = result.copyWithCount(minCrafts * result.getCount());
    }

    public int getMinCrafts() {
        return minCrafts;
    }

    public ItemStack getResult() {
        return result;
    }

    public boolean isRemoved() {
        return stacks.stream().anyMatch(ItemEntity::isRemoved);
    }

    public void consumeInputs(int crafts) {
        for (int itemId : new IntOpenHashSet(this.inputs.keySet())) {
            consumeInput(itemId, crafts);
        }
    }

    public void consumeInput(int itemId, int amount) {
        inputs.compute(itemId, (i, entities) -> entities == null ? null : entities.consume(amount));
    }

    public void craft(Level world) {
        ItemEntity output = new ItemEntity(world, position.x(), position.y(), position.z(), result);
        output.setPermanentlyInvulnerable(true);
        consumeInputs(minCrafts);
        world.addFreshEntity(output);
    }

    class ItemInput {
        int count;
        final List<ItemEntity> entities = new ArrayList<>();

        public void add(ItemEntity stack) {
            count += stack.getItem().getCount();
            entities.add(stack);
        }

        @Nullable
        public ItemInput consume(int amount) {
            if (entities.isEmpty()) {
                return null;
            }
            count = Math.max(0, count - amount);
            int remainder = amount;
            while (remainder > 0 && !entities.isEmpty()) {
                ItemEntity entity = entities.get(0);
                ItemStack stack = entity.getItem();
                int decremented = Math.min(remainder, stack.getCount());
                remainder -= decremented;
                stack.shrink(decremented);
                if (stack.isEmpty()) {
                    entity.discard();
                    entities.remove(0);
                } else {
                    entity.setItem(stack);
                }
            }
            if (entities.isEmpty()) {
                return null;
            }

            return this;
        }
    }
}
