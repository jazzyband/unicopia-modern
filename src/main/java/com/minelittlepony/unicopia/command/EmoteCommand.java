package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class EmoteCommand {
    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands
                .literal("emote")
                .then(Commands.argument("animation", Animation.argument()).executes(source -> apply(
                        source.getSource(),
                        source.getArgument("animation", Animation.class),
                        Animation.Recipient.ANYONE
                    )
                ).then(Commands.argument("duration", IntegerArgumentType.integer(1, 99)).executes(source -> apply(
                        source.getSource(),
                        source.getArgument("animation", Animation.class),
                        Animation.Recipient.ANYONE,
                        IntegerArgumentType.getInteger(source, "duration")
                    )
                ).then(Commands.argument("recipient_type", Animation.Recipient.argument()).executes(source -> apply(
                        source.getSource(),
                        source.getArgument("animation", Animation.class),
                        source.getArgument("recipient_type", Animation.Recipient.class),
                        IntegerArgumentType.getInteger(source, "duration")
                    )
                ))
        ));
    }

    static int apply(CommandSourceStack source, Animation animation, Animation.Recipient recipient) throws CommandSyntaxException {
        return apply(source, animation, recipient, animation.getDuration());
    }

    static int apply(CommandSourceStack source, Animation animation, Animation.Recipient recipient, int duration) throws CommandSyntaxException {
        Pony.of(source.getPlayer()).setAnimation(animation, recipient, duration);
        return 0;
    }
}
