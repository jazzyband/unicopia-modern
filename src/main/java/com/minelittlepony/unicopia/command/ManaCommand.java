package com.minelittlepony.unicopia.command;

import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

public class ManaCommand {
    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("mana").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.argument("type", ManaType.argument()).executes(source -> {
                var type = source.getArgument("type", ManaType.class);
                var pony = Pony.of(source.getSource().getPlayer());
                var bar = type.getBar(pony);

                source.getSource().sendSuccess(() -> Component.literal(type.name() + " is " + Either.unwrap(bar.mapBoth(
                        left -> left.get() + "/" + left.getMax(),
                        right -> right.get() + "/" + right.getMax()))), true);
                return 0;
            })
            .then(Commands.argument("value", FloatArgumentType.floatArg()).executes(source -> {
                var type = source.getArgument("type", ManaType.class);
                var pony = Pony.of(source.getSource().getPlayer());
                float value = source.getArgument("value", Float.class);

                var answer = type.getBar(pony).mapBoth(left -> {
                    left.set((int)value);
                    return left.get() + "/" + left.getMax();
                }, right -> {
                    right.set(value);
                    return right.get() + "/" + right.getMax();
                });
                pony.asWorld().playSound(null, pony.getOrigin(), USounds.Vanilla.PLAYER_LEVELUP, SoundSource.PLAYERS, 1, 2);

                source.getSource().sendSuccess(() -> Component.literal("Set " + type.name() + " to " + Either.unwrap(answer)), true);
                return 0;
            })));
    }

    enum ManaType implements CommandArgumentEnum<ManaType> {
        EXERTION(pony -> Either.right(pony.getMagicalReserves().getExertion())),
        EXHAUSTION(pony -> Either.right(pony.getMagicalReserves().getExhaustion())),
        ENERGY(pony -> Either.right(pony.getMagicalReserves().getEnergy())),
        MANA(pony -> Either.right(pony.getMagicalReserves().getMana())),
        XP(pony -> Either.right(pony.getMagicalReserves().getXp())),
        LEVEL(pony -> Either.left(pony.getLevel())),
        CORRUPTION(pony -> Either.left(pony.getCorruption()));

        private final Function<Pony, Either<Levelled.LevelStore, MagicReserves.Bar>> getter;

        ManaType(Function<Pony, Either<Levelled.LevelStore, MagicReserves.Bar>> getter) {
            this.getter = getter;
        }

        public Either<Levelled.LevelStore, MagicReserves.Bar> getBar(Pony pony) {
            return getter.apply(pony);
        }

        public static StringRepresentableArgument<ManaType> argument() {
            return new ArgumentType();
        }

        public static final class ArgumentType extends StringRepresentableArgument<ManaType> {
            static final Codec<ManaType> CODEC = StringRepresentable.fromEnum(ManaType::values);

            protected ArgumentType() {
                super(CODEC, ManaType::values);
            }
        }
    }
}
