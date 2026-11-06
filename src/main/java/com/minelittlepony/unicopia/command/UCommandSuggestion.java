package com.minelittlepony.unicopia.command;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

public class UCommandSuggestion {
    public static final SuggestionProvider<CommandSourceStack> ALL_RACE_SUGGESTIONS = suggestFromRegistry(Race.REGISTRY_KEY);
    public static final SuggestionProvider<CommandSourceStack> ALLOWED_RACE_SUGGESTIONS = suggestFromRegistry(Race.REGISTRY_KEY, (context, race) -> race.availability().isGrantable() && race.isPermitted(context.getSource().getPlayer()));

    public static <T> SuggestionProvider<CommandSourceStack> suggestFromRegistry(ResourceKey<? extends Registry<T>> registryKey, @Nullable BiPredicate<CommandContext<CommandSourceStack>, T> filter) {
        return (context, builder) -> {
            Registry<T> registry = context.getSource().registryAccess().lookupOrThrow(registryKey);
            return suggestIdentifiers(
                    filter == null ? registry : registry.stream().filter(v -> filter.test(context, v))::iterator,
                    registry::getKey,
                    builder, registryKey.identifier().getNamespace());
        };
    }

    public static <T> SuggestionProvider<CommandSourceStack> suggestFromRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        return suggestFromRegistry(registryKey, null);
    }

    public static <T> CompletableFuture<Suggestions> suggestIdentifiers(Iterable<T> candidates, Function<T, Identifier> idFunc, SuggestionsBuilder builder, String defaultNamespace) {
        forEachMatching(candidates, builder.getRemaining().toLowerCase(Locale.ROOT), idFunc, id -> builder.suggest(idFunc.apply(id).toString()), defaultNamespace);
        return builder.buildFuture();
    }

    public static <T> void forEachMatching(Iterable<T> candidates, String input, Function<T, Identifier> idFunc, Consumer<T> consumer, String defaultNamespace) {
        final boolean hasNamespaceDelimiter = input.indexOf(':') > -1;
        for (T object : candidates) {
            final Identifier id = idFunc.apply(object);
            if (hasNamespaceDelimiter) {
                if (SharedSuggestionProvider.matchesSubStr(input, id.toString())) {
                    consumer.accept(object);
                }
            } else {
                if (SharedSuggestionProvider.matchesSubStr(input, id.getNamespace())
                    || (id.getNamespace().equals(defaultNamespace) && SharedSuggestionProvider.matchesSubStr(input, id.getPath()))
                ) {
                    consumer.accept(object);
                }
            }
        }
    }
}
