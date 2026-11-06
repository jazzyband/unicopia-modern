package com.minelittlepony.unicopia.command;

import java.util.function.Function;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.DispersableDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;

public class DisguiseCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.disguise.notfound"));

    public static LiteralArgumentBuilder<CommandSourceStack> create(CommandBuildContext registries) {
        return Commands.literal("disguise").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .executes(context -> reveal(context.getSource(), context.getSource().getPlayer()))
            .then(
                Commands.argument("target", EntityArgument.players())
                .then(buildEntityDisguise(context -> EntityArgument.getPlayer(context, "target"), registries))
                .then(buildPlayerDisguise(context -> EntityArgument.getPlayer(context, "target")))
            )
            .then(buildEntityDisguise(context -> context.getSource().getPlayer(), registries))
            .then(buildPlayerDisguise(context -> context.getSource().getPlayer()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> buildEntityDisguise(Arg<ServerPlayer> targetOp, CommandBuildContext registries) {
        return Commands.argument("entity", ResourceArgument.resource(registries, Registries.ENTITY_TYPE))
                    .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                    .executes(context -> disguise(
                        context.getSource(),
                        targetOp.apply(context),
                        loadEntity(context.getSource(),
                            ResourceArgument.getSummonableEntityType(context, "entity"),
                            new CompoundTag())))
        .then(
                Commands.argument("nbt", CompoundTagArgument.compoundTag())
                    .executes(context -> disguise(
                        context.getSource(),
                        targetOp.apply(context),
                        loadEntity(context.getSource(),
                            ResourceArgument.getSummonableEntityType(context, "entity"),
                            CompoundTagArgument.getCompoundTag(context, "nbt"))))
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> buildPlayerDisguise(Arg<ServerPlayer> targetOp) {
        return Commands.argument("playername", StringArgumentType.string())
                    .executes(context -> disguise(
                        context.getSource(),
                        targetOp.apply(context),
                        loadPlayer(context.getSource(), StringArgumentType.getString(context, "playername"))));
    }

    static int disguise(CommandSourceStack source, Player player, Entity entity) throws CommandSyntaxException {
        if (entity == null || !EquinePredicates.VALID_FOR_DISGUISE.test(entity)) {
            throw FAILED_EXCEPTION.create();
        }

        Pony iplayer = Pony.of(player);
        iplayer.getSpellSlot().get(SpellType.CHANGELING_DISGUISE)
            .orElseGet(() -> {
                DispersableDisguiseSpell spell = SpellType.CHANGELING_DISGUISE.withTraits().apply(iplayer, CastingMethod.INNATE);
                if (!iplayer.canUse(Abilities.DISGUISE)) {
                    spell.setForced();
                }
                return spell;
            })
            .setDisguise(entity);

        if (source.getEntity() == player) {
            source.sendSuccess(() -> Component.translatable("commands.disguise.success.self", entity.getName()), true);
        } else {
            if (player.level().getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendSystemMessage(Component.translatable("commands.disguise.success", entity.getName()));
            }

            source.sendSuccess(() -> Component.translatable("commands.disguise.success.other", player.getName(), entity.getName()), true);
        }

        return 0;
    }

    static Entity loadEntity(CommandSourceStack source, Holder.Reference<EntityType<?>> entityType, CompoundTag nbt) {
        nbt = nbt.copy();
        nbt.putString("id", entityType.key().identifier().toString());
        return EntityType.loadEntityRecursive(TagValueInput.create(ProblemReporter.DISCARDING, source.getLevel().registryAccess(), nbt),
                source.getLevel(), EntitySpawnReason.MOB_SUMMONED, entity -> entity);
    }

    static Entity loadPlayer(CommandSourceStack source, String username) {
        return InteractionManager.getInstance().createPlayer(source.getLevel(), new GameProfile(null, username));
    }

    static int reveal(CommandSourceStack source, Player player) {
        Pony iplayer = Pony.of(player);
        iplayer.getSpellSlot().removeIf(SpellPredicate.IS_DISGUISE);

        if (source.getEntity() == player) {
            source.sendSuccess(() -> Component.translatable("commands.disguise.removed.self"), true);
        } else {
            if (player.level().getServer().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendSystemMessage(Component.translatable("commands.disguise.removed"));
            }

            source.sendSuccess(() -> Component.translatable("commands.disguise.removed.other", player.getName()), true);
        }

        return 0;
    }

    interface Arg<T> {
        T apply(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
    }
}
