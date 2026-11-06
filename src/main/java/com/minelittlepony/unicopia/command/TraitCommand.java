package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;

class TraitCommand {
    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("trait").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("add")
                    .then(Commands.argument("trait", Trait.argument())
                    .then(Commands.argument("value", FloatArgumentType.floatArg()).executes(source -> add(
                            source.getSource(),
                            source.getSource().getPlayer(),
                            source.getArgument("trait", Trait.class),
                            FloatArgumentType.getFloat(source, "value")
                    )))
            ))
            .then(Commands.literal("remove")
                    .then(Commands.argument("trait", Trait.argument()).executes(source -> remove(
                            source.getSource(),
                            source.getSource().getPlayer(),
                            source.getArgument("trait", Trait.class)
                    ))
            ));
    }

    static int add(CommandSourceStack source, Player player, Trait trait, float amount) throws CommandSyntaxException {
        if (trait == null) {
            source.sendFailure(Component.literal("Invalid trait"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("That trait cannot be added to the current item"));
            return 0;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, SpellTraits.union(SpellTraits.of(stack), SpellTraits.of(trait, amount)).applyTo(stack));

        return 0;
    }

    static int remove(CommandSourceStack source, Player player, Trait trait) throws CommandSyntaxException {
        if (trait == null) {
            source.sendFailure(Component.literal("Invalid trait"));
            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        SpellTraits existing = SpellTraits.of(stack);
        if (existing.get(trait) == 0) {
            return 0;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, existing.map((t, v) -> t == trait ? 0 : v).applyTo(stack));

        return 0;
    }

    static int get(CommandSourceStack source, Player player, Trait trait, float amount) throws CommandSyntaxException {
        String translationKey = "commands.gravity.get";

        Pony iplayer = Pony.of(player);

        float gravity = iplayer.getPhysics().getGravityModifier();

        if (source.getPlayer() == player) {
            if (player.level().getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendSystemMessage(Component.translatable(translationKey, gravity));
            }
        } else {
            source.sendSuccess(() -> Component.translatable(translationKey + ".other", player.getName(), gravity), true);
        }

        return 0;
    }
}
