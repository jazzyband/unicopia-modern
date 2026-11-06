package com.minelittlepony.unicopia.block.state;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface ReversableStateChange {
    Codec<ReversableStateChange> CODEC = Identifier.CODEC.dispatch(ReversableStateChange::getType, type -> ReversableStateChange.REGISTRY.get(type));
    Map<Identifier, MapCodec<? extends ReversableStateChange>> REGISTRY = Map.of(
            Chance.ID, Chance.MAP_CODEC,
            SetRandomState.ID, SetRandomState.CODEC,
            SetState.ID, SetState.CODEC,
            SetProperty.ID, SetProperty.CODEC,
            CycleProperty.ID, CycleProperty.CODEC
    );

    static void bootstrap() { }

    Identifier getType();

    Optional<@NotNull BlockState> getConverted(Level world, @NotNull BlockState state);

    default Optional<StatePredicate> getInverse() {
        return getInverse(this);
    }

    default Optional<StatePredicate> getInverse(ReversableStateChange self) {
        return Optional.empty();
    }

    record Chance(ReversableStateChange change, float chance) implements ReversableStateChange {
        public static final Identifier ID = Unicopia.id("chance");
        public static final MapCodec<Chance> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ReversableStateChange.CODEC.fieldOf("change").forGetter(Chance::change),
                Codec.FLOAT.fieldOf("chance").forGetter(Chance::chance)
        ).apply(i, Chance::new));

        @Override
        public Optional<StatePredicate> getInverse(ReversableStateChange self) {
            return change.getInverse(self);
        }

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public Optional<@NotNull BlockState> getConverted(Level world, @NotNull BlockState state) {
            if (world.getRandom().nextFloat() > chance) {
                return Optional.empty();
            }
            return change.getConverted(world, state);
        }
    }

    record SetRandomState(StatePredicate.Tag tag) implements ReversableStateChange {
        public static final Identifier ID = Unicopia.id("set_random_state");
        public static final MapCodec<SetRandomState> CODEC = StatePredicate.Tag.MAP_CODEC.xmap(SetRandomState::new, SetRandomState::tag);

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public Optional<StatePredicate> getInverse(ReversableStateChange self) {
            return Optional.of(new StatePredicate.Tag(tag.tag(), Optional.of(self)));
        }

        @Override
        public Optional<@NotNull BlockState> getConverted(Level world, @NotNull BlockState state) {
            return BuiltInRegistries.BLOCK.getRandomElementOf(tag.tag(), world.getRandom())
                    .map(Holder::value)
                    .map(Block::defaultBlockState)
                    .map(newState -> StateUtil.copyState(state, newState));
        }
    }

    record SetState(StatePredicate.State state) implements ReversableStateChange {
        public static final Identifier ID = Unicopia.id("set_state");
        public static final MapCodec<SetState> CODEC = StatePredicate.State.MAP_CODEC.xmap(SetState::new, SetState::state);

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public Optional<StatePredicate> getInverse(ReversableStateChange self) {
            return Optional.of(new StatePredicate.State(state.id(), state.properties(), Optional.of(self)));
        }

        @Override
        public Optional<@NotNull BlockState> getConverted(Level world, @NotNull BlockState state) {
            return BuiltInRegistries.BLOCK.getOptional(this.state.id()).map(Block::defaultBlockState)
                    .map(newState -> this.state.applyTo(world, StateUtil.copyState(state, newState)));
        }
    }

    record SetProperty(String property, String value) implements ReversableStateChange {
        public static final Identifier ID = Unicopia.id("set_property");
        public static final MapCodec<SetProperty> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("property").forGetter(SetProperty::property),
                Codec.STRING.fieldOf("value").forGetter(SetProperty::property)
        ).apply(i, SetProperty::new));

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public Optional<@NotNull BlockState> getConverted(Level world, @NotNull BlockState state) {
            return StatePredicate.getProperty(state, property)
                    .flatMap(property -> property.getValue(value).map(v -> state.setValue(property, v)));
        }
    }

    record CycleProperty(String property) implements ReversableStateChange {
        public static final Identifier ID = Unicopia.id("cycle_property");
        public static final MapCodec<CycleProperty> CODEC = Codec.STRING.fieldOf("property").xmap(CycleProperty::new, CycleProperty::property);

        @Override
        public Identifier getType() {
            return ID;
        }

        @Override
        public Optional<@NotNull BlockState> getConverted(Level world, @NotNull BlockState state) {
            return StatePredicate.getProperty(state, property).map(property -> state.cycle(property));
        }
    }
}
