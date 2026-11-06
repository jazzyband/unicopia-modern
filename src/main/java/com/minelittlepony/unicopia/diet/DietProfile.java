package com.minelittlepony.unicopia.diet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.component.TransientComponentMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DietProfile(
        float defaultMultiplier,
        float foragingMultiplier,
        List<Multiplier> multipliers,
        List<FoodGroupEffects> effects,
        Optional<FoodGroupEffects> defaultEffect
    ) {
    public static final DietProfile EMPTY = new DietProfile(1, 1, List.of(), List.of(), Optional.empty());
    public static final Codec<DietProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("default_multiplier").forGetter(DietProfile::defaultMultiplier),
                Codec.FLOAT.fieldOf("foraging_multiplier").forGetter(DietProfile::foragingMultiplier),
                Codec.list(Multiplier.CODEC).fieldOf("multipliers").forGetter(DietProfile::multipliers),
                Codec.list(FoodGroupEffects.createCodec(FoodGroupKey.CODEC)).fieldOf("effects").forGetter(DietProfile::effects),
                FoodGroupEffects.createCodec(FoodGroupKey.CODEC).optionalFieldOf("default_effect").forGetter(DietProfile::defaultEffect)
    ).apply(instance, DietProfile::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DietProfile> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, DietProfile::defaultMultiplier,
            ByteBufCodecs.FLOAT, DietProfile::foragingMultiplier,
            Multiplier.PACKET_CODEC.apply(ByteBufCodecs.list()), DietProfile::multipliers,
            FoodGroupEffects.createPacketCodec(FoodGroupKey.PACKET_CODEC).apply(ByteBufCodecs.list()), DietProfile::effects,
            ByteBufCodecs.optional(FoodGroupEffects.createPacketCodec(FoodGroupKey.PACKET_CODEC)), DietProfile::defaultEffect,
            DietProfile::new
    );

    public void validate(Consumer<String> issues, Predicate<Identifier> foodGroupExists) {
        multipliers.stream().flatMap(i -> i.tags().stream()).forEach(key -> {
            if (!foodGroupExists.test(key.id())) {
                issues.accept("Multiplier referenced unknown food group: " + key.id());
            }
        });
        effects.stream().flatMap(i -> i.tags().stream()).forEach(key -> {
            if (!foodGroupExists.test(key.id())) {
                issues.accept("Override defined for unknown food group: " + key.id());
            }
        });
        defaultEffect.stream().flatMap(i -> i.tags().stream()).forEach(key -> {
            if (!foodGroupExists.test(key.id())) {
                issues.accept("Default override defined for unknown food group: " + key.id());
            }
        });
    }

    public Optional<Multiplier> findMultiplier(ItemStack stack) {
        return multipliers.stream().filter(m -> m.test(stack)).findFirst();
    }

    public Optional<Effect> findEffect(ItemStack stack) {
        return effects.stream().filter(m -> m.test(stack)).findFirst().or(this::defaultEffect).map(Effect.class::cast);
    }

    static boolean isForaged(ItemStack stack) {
        return stack.getComponents().get(DataComponents.FOOD) == null;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean hasFoodAttributes(ItemStack stack, TransientComponentMap components, boolean original) {
        if (isEmpty()) {
            return original;
        }

        return !isInedible(stack) && (original || getFoodAttributeForNonEdibleFood(stack, components).isPresent());
    }

    @Nullable
    public FoodProperties getAdjustedFoodComponent(ItemStack stack, TransientComponentMap components, @Nullable FoodProperties food) {
        if (isEmpty()) {
            return food;
        }

        if (food != null && (food.nutrition() > 0 || food.saturation() > 0)) {
            return applyFoodQualityRatio(stack, food);
        }

        return getFoodAttributeForNonEdibleFood(stack, components).map(FoodAttributes::food).orElse(food);
    }

    @Nullable
    public Consumable getAdjustedConsumableComponent(ItemStack stack, TransientComponentMap components, @Nullable Consumable consumable) {
        return getFoodAttributeForNonEdibleFood(stack, components)
                .map(attributes -> attributes.getConsumableComponent(consumable))
                .orElse(consumable);
    }

    @Nullable
    private FoodProperties applyFoodQualityRatio(ItemStack stack, FoodProperties food) {
        var ratios = getRatios(stack);
        if (isInedible(ratios)) {
            return null;
        }

        float hunger = food.nutrition() * ratios.getFirst();
        int baseline = (int)hunger;

        return new FoodProperties(
            Math.max(1, (hunger - baseline) >= 0.5F ? baseline + 1 : baseline),
            food.saturation() * ratios.getSecond(),
            food.canAlwaysEat()
        );
    }

    public boolean isInedible(ItemStack stack) {
        return isInedible(getRatios(stack));
    }

    public static boolean isInedible(Pair<Float, Float> ratios) {
        return ratios.getFirst() <= 0.01F && ratios.getSecond() <= 0.01F;
    }

    private Pair<Float, Float> getRatios(ItemStack stack) {
        Optional<Multiplier> multiplier = findMultiplier(stack);

        float baseMultiplier = (isForaged(stack) ? foragingMultiplier() : defaultMultiplier());
        float hungerMultiplier = multiplier.map(Multiplier::hunger).orElse(baseMultiplier);
        float saturationMultiplier = multiplier.map(Multiplier::saturation).orElse(baseMultiplier);
        return Pair.of(hungerMultiplier, saturationMultiplier);
    }

    public void appendTooltip(ItemStack stack, @Nullable Pony pony, Consumer<Component> tooltip, TooltipFlag context) {
        if (isEmpty()) {
            return;
        }

        var food = stack.get(DataComponents.FOOD);
        var ratios = getRatios(stack);

        if (food == null || isInedible(ratios)) {
            return;
        }

        tooltip.accept(Component.translatable("unicopia.diet.information").withStyle(ChatFormatting.DARK_PURPLE));
        findEffect(stack).orElseGet(() -> PonyDiets.getInstance().getEffects(stack)).appendTooltip(stack, tooltip, context);

        float baseMultiplier = (isForaged(stack) ? foragingMultiplier() : defaultMultiplier());

        if (context.isAdvanced()) {
            var nonAdjustedFood = getNonAdjustedFoodComponent(stack, pony).orElse(food);
            tooltip.accept(Component.literal(" ").append(Component.translatable("unicopia.diet.base_multiplier", baseMultiplier).withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.accept(Component.literal(" ").append(Component.translatable("unicopia.diet.hunger.detailed", food.nutrition(), nonAdjustedFood.nutrition(), (int)(ratios.getFirst() * 100))).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.literal(" ").append(Component.translatable("unicopia.diet.saturation.detailed", food.saturation(), nonAdjustedFood.saturation(), (int)(ratios.getSecond() * 100))).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.literal(" ").append(Component.translatable("unicopia.diet.hunger", (int)(ratios.getFirst() * 100))).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.literal(" ").append(Component.translatable("unicopia.diet.saturation", (int)(ratios.getSecond() * 100))).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private Optional<FoodProperties> getNonAdjustedFoodComponent(ItemStack stack, @Nullable Pony pony) {
        FoodProperties food = stack.getComponents().get(DataComponents.FOOD);

        if (food != null) {
            return Optional.ofNullable(food);
        }

        if (pony != null && pony.getObservedSpecies().hasIronGut()) {
            return findEffect(stack)
                .flatMap(Effect::foodAttributes)
                .or(() -> PonyDiets.getInstance().getEffects(stack).foodAttributes())
                .map(FoodAttributes::food);
        }

        return Optional.empty();
    }

    private Optional<FoodAttributes> getFoodAttributeForNonEdibleFood(ItemStack stack, TransientComponentMap components) {
        if (isEmpty()) {
            return Optional.empty();
        }
        if (components.getCarrier()
                .flatMap(Pony::of)
                .filter(pony -> pony.getObservedSpecies().hasIronGut())
                .isEmpty()) {
            return Optional.empty();
        }

        return findEffect(stack)
                .flatMap(Effect::foodAttributes)
                .or(() -> PonyDiets.getInstance().getEffects(stack).foodAttributes());
    }

    public record Multiplier(
            Set<FoodGroupKey> tags,
            float hunger,
            float saturation
    ) implements Predicate<ItemStack> {
        public static final Codec<Set<FoodGroupKey>> TAGS_CODEC = FoodGroupKey.CODEC.listOf().xmap(
                l -> l.stream().distinct().collect(Collectors.toSet()),
                set -> new ArrayList<>(set)
        );
        public static final Codec<Multiplier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TAGS_CODEC.fieldOf("tags").forGetter(Multiplier::tags),
                Codec.FLOAT.fieldOf("hunger").forGetter(Multiplier::hunger),
                Codec.FLOAT.fieldOf("saturation").forGetter(Multiplier::saturation)
        ).apply(instance, Multiplier::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Multiplier> PACKET_CODEC = StreamCodec.composite(
                FoodGroupKey.PACKET_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), Multiplier::tags,
                ByteBufCodecs.FLOAT, Multiplier::hunger,
                ByteBufCodecs.FLOAT, Multiplier::saturation,
                Multiplier::new
        );

        @Override
        public boolean test(ItemStack stack) {
            return tags.stream().anyMatch(tag -> tag.contains(stack));
        }

        public static final class Builder {
            private Set<FoodGroupKey> tags = new HashSet<>();
            private float hunger = 1;
            private float saturation = 1;

            public Builder tag(Identifier tag) {
                tags.add(FoodGroupKey.LOOKUP.apply(tag));
                return this;
            }

            public Builder hunger(float hunger) {
                this.hunger = hunger;
                return this;
            }

            public Builder saturation(float saturation) {
                this.saturation = saturation;
                return this;
            }

            public Multiplier build() {
                return new Multiplier(tags, hunger, saturation);
            }
        }
    }
}
