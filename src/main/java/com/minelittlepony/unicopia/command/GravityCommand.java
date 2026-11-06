package com.minelittlepony.unicopia.command;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import com.minelittlepony.unicopia.entity.Living;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

class GravityCommand {

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("gravity").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("get")
                            .executes(context -> get(context.getSource(), context.getSource().getPlayer(), true))
                   .then(Commands.argument("target", EntityArgument.entity())
                            .executes(context -> get(context.getSource(), EntityArgument.getEntity(context, "target"), false))
                    ))
            .then(Commands.literal("set")
                   .then(Commands.argument("gravity", FloatArgumentType.floatArg(-99, 99))
                               .executes(context -> set(context.getSource(), List.of(context.getSource().getPlayer()), FloatArgumentType.getFloat(context, "gravity"), true))
                   .then(Commands.argument("target", EntityArgument.entities())
                               .executes(context -> set(context.getSource(), EntityArgument.getEntities(context, "target"), FloatArgumentType.getFloat(context, "gravity"), false))
                   )));
    }

    static int get(CommandSourceStack source, Entity target, boolean isSelf) throws CommandSyntaxException {
        Living<?> l = Living.living(target);

        float gravity = l == null ? 1 : l.getPhysics().getGravityModifier();
        if (source.getEntity() == target) {
            source.sendSuccess(() -> Component.translatable("commands.gravity.get.self", gravity), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.gravity.get.other", target.getDisplayName(), gravity), true);
        }
        return 0;
    }

    static int set(CommandSourceStack source, Collection<? extends Entity> targets, float gravity, boolean isSelf) {
        List<Entity> affected = targets.stream().map(Living::living).filter(Objects::nonNull).map(l -> {
            l.getPhysics().setBaseGravityModifier(gravity);
            if (l.asEntity() instanceof Player player) {
                if (source.getEntity() == player) {
                    source.sendSuccess(() -> Component.translatable("commands.gravity.set.self", gravity), true);
                } else {
                    if (source.getLevel().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK)) {
                        player.sendSystemMessage(Component.translatable("commands.gravity.set", gravity));
                    }

                    source.sendSuccess(() -> Component.translatable("commands.gravity.set.other", l.asEntity().getDisplayName(), gravity), true);
                }
            }
            return (Entity)l.asEntity();
        }).toList();

        if (affected.size() > 1) {
            source.sendSuccess(() -> Component.translatable("commands.gravity.set.multiple", affected.size()), true);
        }
        return 0;
    }
}
