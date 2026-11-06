package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.Composite;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe.CraftingTreeBuilder;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.item.UItems;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SpellbookRecipeDisplay(List<Input> inputs, List<List<ItemStack>> results, SlotDisplay result) implements RecipeDisplay {
    @SuppressWarnings("deprecation")
    static final SlotDisplay CRAFTING_STATION = new ItemSlotDisplay(UItems.SPELLBOOK.builtInRegistryHolder());

    private static final MapCodec<SpellbookRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Input.CODEC.listOf().fieldOf("inputs").forGetter(SpellbookRecipeDisplay::inputs),
        ItemStack.CODEC.listOf().listOf().fieldOf("results").forGetter(SpellbookRecipeDisplay::results),
        SlotDisplay.CODEC.fieldOf("result").forGetter(SpellbookRecipeDisplay::result)
    ).apply(i, SpellbookRecipeDisplay::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, SpellbookRecipeDisplay> PACKET_CODEC = StreamCodec.composite(
        Input.PACKET_CODEC.apply(ByteBufCodecs.list()), SpellbookRecipeDisplay::inputs,
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()), SpellbookRecipeDisplay::results,
        SlotDisplay.STREAM_CODEC, SpellbookRecipeDisplay::result,
        SpellbookRecipeDisplay::new
    );
    public static final Type<SpellbookRecipeDisplay> SERIALIZER = new Type<>(CODEC, PACKET_CODEC);

    public static SpellbookRecipeDisplay of(Consumer<CraftingTreeBuilder> consumer) {
        List<Input> inputs = new ArrayList<>();
        List<List<ItemStack>> results = new ArrayList<>();
        consumer.accept(new CraftingTreeBuilder() {
            @Override
            public void input(ItemStack... stacks) {
                inputs.add(new Input(false, Optional.of(List.of(stacks)), Optional.empty(), Optional.empty()));
            }

            @Override
            public void input(Trait... traits) {
                inputs.add(new Input(false, Optional.empty(), Optional.of(List.of(traits)), Optional.empty()));
            }

            @Override
            public void input(Trait trait, float value) {
                inputs.add(new Input(false, Optional.empty(), Optional.empty(), Optional.of(Pair.of(trait, value))));
            }

            @Override
            public void mystery(ItemStack...stacks) {
                inputs.add(new Input(true, Optional.of(List.of(stacks)), Optional.empty(), Optional.empty()));
            }

            @Override
            public void result(ItemStack... stack) {
                results.add(List.of(stack));
            }
        });

        return new SpellbookRecipeDisplay(inputs, results, new Composite(results.stream()
                .map(stacks -> (SlotDisplay)(stacks.size() == 1 ? new ItemStackSlotDisplay(net.minecraft.world.item.ItemStackTemplate.fromStack(stacks.get(0))) :  new Composite(stacks.stream().map(i -> (SlotDisplay)new ItemStackSlotDisplay(net.minecraft.world.item.ItemStackTemplate.fromStack(i))).toList())))
                .toList()));
    }

    @Override
    public SlotDisplay craftingStation() {
        return CRAFTING_STATION;
    }

    @Override
    public Type<? extends RecipeDisplay> type() {
        return SERIALIZER;
    }

    public void buildCraftingTree(CraftingTreeBuilder builder) {
        inputs.forEach(input -> {
            input.stacks().ifPresent(stacks -> {
                if (input.hidden()) {
                    builder.mystery(stacks);
                } else {
                    builder.input(stacks);
                }
            });
            input.traits().ifPresent(traits -> {
                builder.input(traits.stream().toArray(Trait[]::new));
            });
            input.trait().ifPresent(trait -> {
                builder.input(trait.getFirst(), trait.getSecond());
            });
        });
        results.forEach(builder::result);
    }

    public record Input(
            boolean hidden,
            Optional<List<ItemStack>> stacks,
            Optional<List<Trait>> traits,
            Optional<Pair<Trait, Float>> trait) {

        private static final Codec<Pair<Trait, Float>> TRAIT_PAIR_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Trait.CODEC.fieldOf("trait").forGetter(Pair::getFirst),
            Codec.FLOAT.fieldOf("value").forGetter(Pair::getSecond)
        ).apply(instance, Pair::of));
        private static final StreamCodec<RegistryFriendlyByteBuf, Pair<Trait, Float>> TRAIT_PAIR_PACKET_CODEC = StreamCodec.composite(
                Trait.PACKET_CODEC, Pair::getFirst,
                ByteBufCodecs.FLOAT, Pair::getSecond,
                Pair::of
        );

        private static final Codec<Input> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("hidden").forGetter(Input::hidden),
            ItemStack.CODEC.listOf().optionalFieldOf("stacks").forGetter(Input::stacks),
            Trait.CODEC.listOf().optionalFieldOf("traits").forGetter(Input::traits),
            TRAIT_PAIR_CODEC.optionalFieldOf("trait").forGetter(Input::trait)
        ).apply(i, Input::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, Input> PACKET_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Input::hidden,
                ByteBufCodecs.optional(ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list())), Input::stacks,
                ByteBufCodecs.optional(Trait.PACKET_CODEC.apply(ByteBufCodecs.list())), Input::traits,
                ByteBufCodecs.optional(TRAIT_PAIR_PACKET_CODEC), Input::trait,
                Input::new
        );
    }
}


















