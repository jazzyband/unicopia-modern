package com.minelittlepony.unicopia.command;

import java.util.Optional;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.PlacementControlSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class CastCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(CommandBuildContext registries) {
        return Commands.literal("cast").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .then(
                buildBranches(
                    Commands.argument("spell", ResourceKeyArgument.key(SpellType.REGISTRY_KEY)),
                    c -> Optional.empty()
                )
            )
            .then(
                Commands.argument("spell", ResourceKeyArgument.key(SpellType.REGISTRY_KEY))
                .then(buildBranches(
                        Commands.argument("traits", TraitsArgumentType.traits()),
                        CastCommand::getTraits
                ))
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> buildBranches(ArgumentBuilder<CommandSourceStack, ?> builder,
            TraitsFunc traitsFunc) {
        return builder.then(
                Commands.literal("throw").then(
                    Commands.argument("rot", RotationArgument.rotation()).executes(c -> thrown(c, traitsFunc, 1.5F)).then(
                        Commands.argument("speed", FloatArgumentType.floatArg(0, 10)).executes(c -> thrown(c,
                                traitsFunc,
                                FloatArgumentType.getFloat(c, "speed")
                        ))
                    )
                )
            )
            .then(
                Commands.literal("place").executes(c -> placed(c, traitsFunc, Optional.empty(), c.getSource().getRotation())).then(
                    Commands.argument("loc", BlockPosArgument.blockPos()).executes(c -> placed(c,
                                traitsFunc,
                                Optional.of(Vec3.atCenterOf(BlockPosArgument.getBlockPos(c, "loc"))),
                                c.getSource().getRotation()
                        )).then(
                        Commands.argument("rot", RotationArgument.rotation()).executes(c -> placed(c,
                                traitsFunc,
                                Optional.of(Vec3.atCenterOf(BlockPosArgument.getBlockPos(c, "loc"))),
                                RotationArgument.getRotation(c, "rot").getRotation(c.getSource())
                        ))
                    )
                )
            )
            .then(
                Commands.literal("apply").then(
                    Commands.argument("targets", EntityArgument.entities()).executes(c -> apply(c, traitsFunc))
                )
            );
    }

    private static Optional<SpellTraits> getTraits(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        return Optional.of(TraitsArgumentType.getSpellTraits(source, "traits"));
    }

    private static CustomisedSpellType<?> getSpell(CommandContext<CommandSourceStack> source, TraitsFunc traits) throws CommandSyntaxException {
        SpellType<?> spellType = SpellType.fromArgument(source, "spell");
        return spellType.withTraits(traits.getTraits(source).orElse(spellType.getTraits()));
    }

    private static int thrown(CommandContext<CommandSourceStack> source, TraitsFunc traits, float speed) throws CommandSyntaxException {
        ServerPlayer player = source.getSource().getPlayerOrException();
        Spell spell = getSpell(source, traits).create();
        if (spell != null) {
            spell.toThrowable().throwProjectile(Caster.of(player).orElseThrow()).ifPresent(projectile -> {
                Vec2 rotation = RotationArgument.getRotation(source, "rot").getRotation(source.getSource());
                projectile.shootFromRotation(player, rotation.x, rotation.y, 0, speed, 1);
            });
        }
        return 0;
    }

    private static int placed(CommandContext<CommandSourceStack> source, TraitsFunc traits, Optional<Vec3> position, Vec2 rotation) throws CommandSyntaxException {
        ServerPlayer player = source.getSource().getPlayerOrException();
        Spell spellType = getSpell(source, traits).create();
        if (spellType == null) {
            return 0;
        }
        PlacementControlSpell spell = spellType.toPlaceable();
        Caster<?> caster = Caster.of(player).orElseThrow();

        spell.setOrientation(caster, rotation.x, rotation.y);
        position.ifPresent(spell::setPosition);
        spell.apply(caster);

        return 0;
    }

    private static int apply(CommandContext<CommandSourceStack> source, TraitsFunc traits) throws CommandSyntaxException {
        CustomisedSpellType<?> spellType = getSpell(source, traits);
        EntityArgument.getEntities(source, "targets").forEach(target -> {
            Caster.of(target).ifPresent(caster -> spellType.apply(caster, CastingMethod.INNATE));
        });

        return 0;
    }

    interface TraitsFunc {
        Optional<SpellTraits> getTraits(CommandContext<CommandSourceStack> source) throws CommandSyntaxException;
    }
}
