package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

class SkyAngleCommand {
    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("skyangle").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                    .executes(context -> get(context.getSource()))
            .then(Commands.literal("set")
                    .executes(context -> set(context.getSource(), 0))
                    .then(Commands.argument("angle", FloatArgumentType.floatArg(0, 360))
                            .executes(context -> set(context.getSource(), FloatArgumentType.getFloat(context, "angle")))));
    }

    static int get(CommandSourceStack source) throws CommandSyntaxException {
        source.sendSuccess(() -> Component.translatable("commands.skyangle.get", UnicopiaWorldProperties.forWorld(source.getLevel()).getTangentalSkyAngle()), true);
        return 0;
    }

    static int set(CommandSourceStack source, float newAngle) {
        UnicopiaWorldProperties.forWorld(source.getLevel()).setTangentalSkyAngle(newAngle);
        source.sendSuccess(() -> Component.translatable("commands.skyangle.set", newAngle), true);
        return 0;
    }

}
