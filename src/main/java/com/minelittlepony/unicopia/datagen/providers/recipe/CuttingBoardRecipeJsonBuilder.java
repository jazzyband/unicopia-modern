package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CuttingBoardRecipeJsonBuilder {
    private final Map<String, Criterion<?>> criterions = new LinkedHashMap<>();

    private final ItemLike output;
    private Either<Ingredient, Tool> tool;

    private final List<Result> results = new ArrayList<>();
    private final List<Ingredient> ingredients = new ArrayList<>();

    private Identifier sound = Identifier.withDefaultNamespace("item.axe.strip");

    public static CuttingBoardRecipeJsonBuilder create(ItemLike output, String action) {
        return new CuttingBoardRecipeJsonBuilder(output).tool(action);
    }

    public static CuttingBoardRecipeJsonBuilder create(ItemLike output, Ingredient tool) {
        return new CuttingBoardRecipeJsonBuilder(output).tool(tool);
    }

    protected CuttingBoardRecipeJsonBuilder(ItemLike output) {
        this.output = output;
    }

    public CuttingBoardRecipeJsonBuilder tool(Ingredient tool) {
        this.tool = Either.left(tool);
        return this;
    }

    public CuttingBoardRecipeJsonBuilder tool(String action) {
        this.tool = Either.right(new Tool(Identifier.parse("farmersdelight:item_ability"), action));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder sound(SoundEvent sound) {
        this.sound = BuiltInRegistries.SOUND_EVENT.getKey(sound);
        return this;
    }

    public CuttingBoardRecipeJsonBuilder input(ItemLike input) {
        ingredients.add(Ingredient.of(input));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder result(ItemLike result, int count) {
        results.add(new Result(BuiltInRegistries.ITEM.getKey(result.asItem()), count));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder result(ItemLike result) {
        return result(result, 1);
    }

    public CuttingBoardRecipeJsonBuilder result(Identifier result) {
        return result(result, 1);
    }

    public CuttingBoardRecipeJsonBuilder result(Identifier result, int count) {
        results.add(new Result(result, count));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder criterion(String name, Criterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> key) {
        key = ResourceKey.create(key.registryKey(), key.identifier().withPrefix("cutting/"));
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + key.identifier());
        Advancement.Builder advancementBuilder = exporter.advancement()
            .rewards(AdvancementRewards.Builder.recipe(key))
            .requirements(AdvancementRequirements.Strategy.OR);
        criterions.forEach(advancementBuilder::addCriterion);
        exporter.accept(key,
            new CuttingBoardRecipe(ingredients, tool, sound, results),
            advancementBuilder.build(key.identifier().withPrefix("recipes/"))
        );
    }

    public void offerTo(RecipeOutput exporter) {
        offerTo(exporter, ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(output.asItem())));
    }

    public void offerTo(RecipeOutput exporter, String recipePath) {
        Identifier outputItemId = BuiltInRegistries.ITEM.getKey(output.asItem());
        Identifier recipeId = outputItemId.withPath(recipePath);
        if (recipeId.equals(outputItemId)) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, ResourceKey.create(Registries.RECIPE, recipeId));
    }

    public record Tool(Identifier type, String action) {
        static final Codec<Tool> CODEC = RecordCodecBuilder.create(ii -> ii.group(
                Identifier.CODEC.fieldOf("fabric:type").forGetter(Tool::type),
                Codec.STRING.fieldOf("action").forGetter(Tool::action)
        ).apply(ii, Tool::new));
    }
    public record Result(Identifier item, int count) {
        private static final Codec<Result> ITEM_CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("id").forGetter(Result::item),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(Result::count)
        ).apply(i, Result::new));
        public static final Codec<Result> CODEC = ITEM_CODEC.fieldOf("item").codec();
    }

    public record CuttingBoardRecipe(
            List<Ingredient> ingredients,
            Either<Ingredient, Tool> tool,
            Identifier sound,
            List<Result> result
        ) implements Recipe<CraftingInput> {
        static final Identifier ID = Identifier.fromNamespaceAndPath("farmersdelight", "cutting");
        static final Codec<Either<Ingredient, Tool>> TOOL_CODEC = Codec.xor(Ingredient.CODEC, Tool.CODEC);
        static final MapCodec<CuttingBoardRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(CuttingBoardRecipe::ingredients),
                TOOL_CODEC.fieldOf("tool").forGetter(CuttingBoardRecipe::tool),
                Identifier.CODEC.fieldOf("sound").forGetter(CuttingBoardRecipe::sound),
                Result.CODEC.listOf().fieldOf("result").forGetter(CuttingBoardRecipe::result)
        ).apply(i, CuttingBoardRecipe::new));
        static final RecipeType<CuttingBoardRecipe> TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE, ID, new RecipeType<>() {
            @Override
            public String toString() {
                return "farmersdelight:cutting";
            }
        });
        static final RecipeSerializer<CuttingBoardRecipe> SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ID, new RecipeSerializer<>(CODEC, null));
        public static void bootstrap() {}

        @Override
        public boolean matches(CraftingInput inventory, Level world) { return false; }

        @Override
        public ItemStack assemble(CraftingInput inventory) { return ItemStack.EMPTY; }

        @Override
        public boolean showNotification() { return false; }

        @Override
        public String group() { return ""; }

        @Override
        public RecipeSerializer<CuttingBoardRecipe> getSerializer() { return SERIALIZER; }

        @Override
        public RecipeType<CuttingBoardRecipe> getType() { return TYPE; }

        @Override
        public PlacementInfo placementInfo() {
            return PlacementInfo.NOT_PLACEABLE;
        }

        @Override
        public RecipeBookCategory recipeBookCategory() {
            return RecipeBookCategories.CRAFTING_MISC;
        }
    }
}
