package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgTribeSelect;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;

class SpeciesCommand {
    static LiteralArgumentBuilder<CommandSourceStack> create(CommandSelection environment) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("tribe");

        if (environment.includeDedicated) {
            if (Unicopia.getConfig().enableCheats.get()) {
                builder = builder.requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER));
            } else {
                builder = builder.requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_OWNER));
            }
        }

        return builder
            .then(Commands.literal("get")
                      .executes(context -> get(context.getSource(), context.getSource().getPlayer(), true))
               .then(Commands.argument("target", EntityArgument.player())
                      .executes(context -> get(context.getSource(), EntityArgument.getPlayer(context, "target"), false))
               ))
            .then(Commands.literal("set")
               .then(Commands.argument("race", Race.argument()).suggests(UCommandSuggestion.ALL_RACE_SUGGESTIONS)
                       .executes(context -> set(context.getSource(), context.getSource().getPlayer(), Race.fromArgument(context, "race"), true))
               .then(Commands.argument("target", EntityArgument.player())
                       .executes(context -> set(context.getSource(), EntityArgument.getPlayer(context, "target"), Race.fromArgument(context, "race"), false)))
               ))
            .then(Commands.literal("describe")
               .then(Commands.argument("race", Race.argument()).suggests(UCommandSuggestion.ALL_RACE_SUGGESTIONS)
                       .executes(context -> describe(context.getSource(), Race.fromArgument(context, "race")))
               ))
            .then(Commands.literal("list")
                       .executes(context -> list(context.getSource())
               ));
    }

    static int set(CommandSourceStack source, Player player, Race race, boolean isSelf) {

        if (race.isPermitted(player)) {
            Pony pony = Pony.of(player);
            pony.setSpecies(race);
            pony.sendUpdatePacket();

            if (race.isUnset()) {
                Channel.SERVER_SELECT_TRIBE.sendToPlayer(new MsgTribeSelect(Race.allPermitted(player), "gui.unicopia.tribe_selection.welcome"), (ServerPlayer)player);
            }

            if (player == source.getPlayer()) {
                source.sendSuccess(() -> Component.translatable("commands.race.success.self", race.getDisplayName()), true);
            } else {
                if (player.level().getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK)) {
                    player.sendSystemMessage(Component.translatable("commands.race.success", race.getDisplayName()));
                }
                source.sendSuccess(() -> Component.translatable("commands.race.success.other", player.getName(), race.getDisplayName()), true);
            }
        } else {
            source.sendSuccess(() -> Component.translatable("commands.race.permission"), false);
        }

        return 0;
    }

    static int get(CommandSourceStack source, Player player, boolean isSelf) {
        source.sendSuccess(() -> {
            Race spec = Pony.of(player).getSpecies();

            String name = "commands.race.tell.";
            name += isSelf ? "self" : "other";

            return Component.translatable(name, player.getName())
                .append(Component.translatable(spec.getTranslationKey())
                        .withStyle(s -> s.withColor(ChatFormatting.GOLD)));
        }, false);
        return 0;
    }

    static int list(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("commands.race.list"), false);
        source.sendSuccess(() -> {
            MutableComponent message = Component.literal("");

            boolean first = true;
            for (Race i : Race.REGISTRY) {
                if (i.availability().isGrantable() && !i.isUnset() && i.isPermitted(source.getPlayer())) {
                    message.append(Component.literal((!first ? "\n" : "") + " - "));
                    message.append(i.getDisplayName());
                    first = false;
                }
            }

            return message.withStyle(s -> s.withColor(ChatFormatting.GOLD));
        }, false);

        return 0;
    }

    static int describe(CommandSourceStack source, Race species) {
        Identifier id = Race.REGISTRY.getKey(species);

        for (String category : new String[] { "goods", "bads" }) {
            source.sendSuccess(() -> Component.translatable(
                    String.format("gui.unicopia.tribe_selection.confirm.%s.%d.%s.%s", category),
                    species.getAltDisplayName()
            ), false);
            for (int i = 1; i < 5; i++) {
                String line = String.format("gui.unicopia.tribe_selection.confirm.%s.%d.%s.%s", category, i, id.getNamespace(), id.getPath());

                source.sendSuccess(() -> Component.translatable(line).withStyle(s -> s.withColor(category.equals("goods") ? ChatFormatting.YELLOW : ChatFormatting.RED)), false);
            }
        }

        return 0;
    }
}
