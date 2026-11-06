package com.minelittlepony.unicopia.command;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import com.minelittlepony.common.util.settings.Setting;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgConfigurationChange;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public class ConfigCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> create(CommandBuildContext registries) {
        return Commands.literal("config").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .then(createSet("dimensionswithoutatmosphere", createSuggestion(registries, Registries.DIMENSION_TYPE)))
            .then(createSet("wantitneeditentityexcludelist", createSuggestion(registries, Registries.ENTITY_TYPE)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSet(String configName, SuggestionProvider<CommandSourceStack> suggestions) {
        return Commands.literal(configName)
            .then(Commands.literal("clear").executes(source -> {
                        source.getSource().sendSuccess(() -> Component.translatable("command.unicopia.config.clear", configName), true);
                        return changeProperty(source.getSource(), configName, values -> new HashSet<>());
                    }))
            .then(Commands.literal("add").then(
                Commands.argument("id", IdentifierArgument.id()).suggests(suggestions).executes(source -> ConfigCommand.<Set<String>>changeProperty(source.getSource(), configName, values -> {
                        String value = IdentifierArgument.getId(source, "id").toString();
                        source.getSource().sendSuccess(() -> Component.translatable("command.unicopia.config.add", value, configName), true);
                        values.add(value);
                        return values;
                    })))
            )
            .then(Commands.literal("remove").then(
                Commands.argument("id", IdentifierArgument.id()).suggests((context, builder) -> {
                    return SharedSuggestionProvider.suggestResource(Unicopia.getConfig().getCategory("server").<Set<String>>get(configName).get().stream()
                            .map(Identifier::tryParse)
                            .filter(Objects::nonNull)
                            .toList(), builder);
                }).executes(source -> ConfigCommand.<Set<String>>changeProperty(source.getSource(), configName, values -> {
                        String value = IdentifierArgument.getId(source, "id").toString();
                        source.getSource().sendSuccess(() -> Component.translatable("command.unicopia.config.remove", value, configName), true);
                        values.remove(value);
                        return values;
                    })))
            )
            .then(Commands.literal("list").executes(source -> ConfigCommand.<Set<String>>getProperty(configName, values -> {
                    source.getSource().sendSuccess(() -> Component.translatable("command.unicopia.config.list", configName, values.size()), false);
                    values.forEach(line -> source.getSource().sendSuccess(() -> Component.literal(line), false));
                }))
            );
    }

    private static <T> SuggestionProvider<CommandSourceStack> createSuggestion(CommandBuildContext registries, ResourceKey<Registry<T>> registryKey) {
        HolderLookup<T> wrapper = registries.lookupOrThrow(registryKey);
        return (context, builder) -> SharedSuggestionProvider.suggestResource(wrapper.listElementIds().map(ResourceKey::identifier), builder);
    }

    private static <T> int changeProperty(CommandSourceStack source, String configName, Function<T, T> changer) {
        Config config = Unicopia.getConfig();
        Setting<T> setting = config.getCategory("server").get(configName);
        setting.set(changer.apply(setting.get()));
        config.save();

        MsgConfigurationChange msg = new MsgConfigurationChange(config.toSynced());
        InteractionManager.getInstance().setSyncedConfig(msg.config());
        source.getServer().getPlayerList().getPlayers().forEach(recipient -> {
            Channel.CONFIGURATION_CHANGE.sendToPlayer(msg, recipient);
        });

        return 0;
    }

    private static <T> int getProperty(String configName, UnsafeConsumer<T> reader) throws CommandSyntaxException {
        Config config = Unicopia.getConfig();
        Setting<T> setting = config.getCategory("server").get(configName);
        reader.accept(setting.get());
        return 0;
    }

    interface UnsafeConsumer<T> {
        void accept(T t) throws CommandSyntaxException;
    }
}
