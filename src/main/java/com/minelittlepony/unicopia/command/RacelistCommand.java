package com.minelittlepony.unicopia.command;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.client.TextHelper;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

class RacelistCommand {

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("racelist").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN))
            .then(Commands.literal("show")
            .executes(context -> {
                context.getSource().sendSuccess(() -> {
                    if (!AllowList.INSTANCE.isEnabled()) {
                        return Component.translatable("commands.racelist.inactive");
                    }
                    Set<MutableComponent> allowed = new HashSet<>();
                    Set<MutableComponent> unallowed = new HashSet<>();
                    Race.REGISTRY.forEach(race -> {
                        (AllowList.INSTANCE.permits(race) ? allowed : unallowed).add(Component.translatable("commands.racelist.get.list_item",
                                race.getDisplayName(),
                                Component.literal(race.getId().toString()).withStyle(ChatFormatting.GRAY)
                        ));
                    });

                    return Component.translatable("commands.racelist.get.allowed", allowed.size()).withStyle(ChatFormatting.YELLOW)
                            .append("\n").append(TextHelper.join(Component.literal("\n"), allowed))
                            .append("\n")
                            .append(Component.translatable("commands.racelist.get.not_allowed", unallowed.size()).withStyle(ChatFormatting.YELLOW))
                            .append("\n").append(TextHelper.join(Component.literal("\n"), unallowed));
                }, false);
                return 0;
            })
            )
            .then(Commands.literal("reset")
            .executes(context -> {
                boolean success = AllowList.INSTANCE.disable();
                context.getSource().sendSuccess(() -> Component.translatable("commands.racelist.reset." + (success ? "success" : "fail")).withStyle(ChatFormatting.YELLOW), false);
                return 0;
            })
            )
            .then(Commands.literal("allow")
                .then(Commands.argument("race", Race.argument()).suggests(UCommandSuggestion.ALL_RACE_SUGGESTIONS)
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), "allowed", AllowList.INSTANCE::add))
            ))
            .then(Commands.literal("disallow")
                .then(Commands.argument("race", Race.argument()).suggests(UCommandSuggestion.ALL_RACE_SUGGESTIONS)
                .executes(context -> toggle(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), "disallowed", AllowList.INSTANCE::remove))
            ));
    }

    static int toggle(CommandSourceStack source, ServerPlayer player, Race race, String action, Function<Race, Boolean> func) {
        boolean enabled = AllowList.INSTANCE.isEnabled();
        boolean success = func.apply(race);

        if (enabled != AllowList.INSTANCE.isEnabled()) {
            source.sendSuccess(() -> Component.translatable("commands.racelist." + (enabled ? "disabled" : "enabled")).withStyle(enabled ? ChatFormatting.RED : ChatFormatting.GREEN), false);
        }

        source.sendSuccess(() -> {
            String translationKey = "commands.racelist." + action;
            if (!success) {
                if (race.isUnset()) {
                    translationKey = "commands.racelist.illegal";
                } else {
                    translationKey += ".failed";
                }
            }

            return Component.translatable(translationKey, race.getDisplayName().copy().withStyle(ChatFormatting.GOLD)).withStyle(success ? ChatFormatting.GREEN : ChatFormatting.RED);
        }, false);
        return 0;
    }
}
