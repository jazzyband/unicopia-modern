package com.minelittlepony.unicopia.diet;

import java.util.function.Function;
import net.minecraft.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

public interface FoodGroupKey {
    Function<Identifier, FoodGroupKey> LOOKUP = Util.memoize(id -> {
        return new FoodGroupKey() {
            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public boolean contains(ItemStack stack) {
                var group = PonyDiets.getEffect(id);
                return group != null && group.test(stack);
            }

            @Override
            public boolean equals(Object o) {
                return o == this && (o instanceof FoodGroupKey k && k.id().equals(id()));
            }

            @Override
            public int hashCode() {
                return id().hashCode();
            }
        };
    });
    Function<TagKey<Item>, FoodGroupKey> TAG_LOOKUP = Util.memoize(tag -> {
        return new FoodGroupKey() {
            private boolean check;
            @Override
            public Identifier id() {
                return tag.location();
            }

            @Override
            public boolean contains(ItemStack stack) {
                if (Debug.CHECK_GAME_VALUES && !check) {
                    check = true;
                    if (BuiltInRegistries.ITEM.get(tag).isEmpty()) {
                        Unicopia.LOGGER.info("Tag is empty: " + tag.location());
                    }
                }

                return stack.is(tag);
            }

            @Override
            public boolean equals(Object o) {
                return o == this && (o instanceof FoodGroupKey k && k.id().equals(id()));
            }

            @Override
            public int hashCode() {
                return id().hashCode();
            }
        };
    });
    Function<Identifier, FoodGroupKey> TAG_ID_LOOKUP = id -> TAG_LOOKUP.apply(TagKey.create(Registries.ITEM, id));

    Codec<FoodGroupKey> CODEC = Identifier.CODEC.xmap(LOOKUP, FoodGroupKey::id);
    Codec<FoodGroupKey> TAG_CODEC = TagKey.codec(Registries.ITEM).xmap(TAG_LOOKUP, k -> TagKey.create(Registries.ITEM, k.id()));

    StreamCodec<ByteBuf, FoodGroupKey> PACKET_CODEC = Identifier.STREAM_CODEC.map(LOOKUP, FoodGroupKey::id);
    StreamCodec<ByteBuf, FoodGroupKey> TAG_PACKET_CODEC = Identifier.STREAM_CODEC.map(id -> TAG_LOOKUP.apply(TagKey.create(Registries.ITEM, id)), FoodGroupKey::id);

    Identifier id();

    boolean contains(ItemStack stack);
}
