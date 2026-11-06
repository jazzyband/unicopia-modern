package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement.Ingredients;
import com.minelittlepony.unicopia.datagen.providers.SpellbookChapterProvider;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderOwner;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class URecipeProvider extends FabricRecipeProvider {
    private final Map<ResourceKey<Recipe<?>>, Holder<Recipe<?>>> recipes = new HashMap<>();
    private final Map<ResourceKey<Recipe<?>>, Ingredients> ingredients = new HashMap<>();

    public URecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Unicopia Recipes";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends Recipe<?>> Holder<T> getOrThrow(ResourceKey<T> key) {
        return Objects.requireNonNull((Holder)recipes.get(key), "Recipe does not exist: " + key);
    }

    public Map<ResourceKey<Recipe<?>>, Ingredients> getIngredients() {
        return Map.copyOf(ingredients);
    }

    @Nullable
    public <T extends Recipe<?>> Ingredients getIngredientsOrThrow(ResourceKey<T> key) {
        return Objects.requireNonNull(ingredients.get(key), "Recipe does not provide spellbook ingredients: " + key);
    }

    @Override
    protected RecipeProvider createRecipeProvider(Provider registryLookup, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        return new URecipeGenerator(registryLookup, recipes, advancements,
                this::withCollecting,
                exporter -> withConditions(exporter, ResourceConditions.allModsLoaded("farmersdelight")));
    }

    protected RecipeOutput withCollecting(RecipeOutput exporter) {
        return new SpellbookChapterProvider.MagicRecipeExporter() {
            @Override
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> registry) {
                return exporter.lookup(registry);
            }

            @Override
            public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> registry) {
                return exporter.listContextElements(registry);
            }

            @Override
            public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, AdvancementHolder advancement) {
                recipes.put(key, new RegistryEntryReference(key, recipe));
                exporter.accept(key, recipe, advancement);
            }

            @Override
            public Builder advancement() {
                return exporter.advancement();
            }

            @Override
            public void acceptChapterIngredients(ResourceKey<Recipe<?>> key, Ingredients value) {
                ingredients.put(key, value);
            }
        };
    }

    class RegistryEntryReference extends Holder.Reference<Recipe<?>> {
        static final HolderOwner<Recipe<?>> OWNER = new HolderOwner<>() {

        };
        protected RegistryEntryReference(ResourceKey<Recipe<?>> registryKey, Recipe<?> value) {
            super(Type.STAND_ALONE, OWNER, registryKey, value);
        }

    }
}
