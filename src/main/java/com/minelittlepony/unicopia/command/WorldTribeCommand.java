package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

class WorldTribeCommand {
    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("worldtribe").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_ADMIN))
            .then(Commands.literal("get").executes(context -> get(context.getSource())))
            .then(Commands.literal("set")
                .then(Commands.argument("race", Race.argument()).suggests(UCommandSuggestion.ALLOWED_RACE_SUGGESTIONS)
                .executes(context -> set(context.getSource(), Race.fromArgument(context, "race")))));
    }

    static int get(CommandSourceStack source) throws CommandSyntaxException {
        source.sendSuccess(() -> {
            UnicopiaWorldProperties manager = UnicopiaWorldProperties.forWorld(source.getLevel());
            return Component.translatable("commands.worldtribe.success.get", manager.getDefaultRace().getDisplayName());
        }, true);
        return 0;
    }

    static int set(CommandSourceStack source, Race race) {
        UnicopiaWorldProperties manager = UnicopiaWorldProperties.forWorld(source.getLevel());
        manager.setDefaultRace(race);

        source.sendSuccess(() -> Component.translatable("commands.worldtribe.success.set", race.getDisplayName()), true);
        return 0;
    }
}
