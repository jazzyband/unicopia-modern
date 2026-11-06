package com.minelittlepony.unicopia.advancement;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class CustomEventCriterion extends AbstractRepeatingCriterion<CustomEventCriterion.Conditions> {
    @Override
    public Codec<com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions> codec() {
        return com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions.CODEC;
    }

    public CustomEventCriterion.Trigger createTrigger(String name) {
        return player -> {
            if (player instanceof ServerPlayer p) {
                trigger(p,
                    condition -> condition.event().equalsIgnoreCase(name),
                    (count, condition) -> condition.test(count, p)
                );
            }
        };
    }

    public interface Trigger {
        void trigger(@Nullable Entity player);
    }

    public static Criterion<?> create(String name) {
        return UCriteria.CUSTOM_EVENT.createCriterion(new com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions(Optional.empty(), name, RacePredicate.EMPTY, TriState.DEFAULT, 1));
    }

    public static Criterion<?> create(String name, int count) {
        return UCriteria.CUSTOM_EVENT.createCriterion(new com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions(Optional.empty(), name, RacePredicate.EMPTY, TriState.DEFAULT, count));
    }

    public static Criterion<?> createFlying(String name) {
        return UCriteria.CUSTOM_EVENT.createCriterion(new com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions(Optional.empty(), name, RacePredicate.EMPTY, TriState.TRUE, 1));
    }

    public static Criterion<?> createFlying(String name, int count) {
        return UCriteria.CUSTOM_EVENT.createCriterion(new com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions(Optional.empty(), name, RacePredicate.EMPTY, TriState.TRUE, count));
    }

    public record Conditions (
            Optional<Holder<LootItemCondition>> player,
            String event,
            RacePredicate races,
            TriState flying,
            int repeatCount) implements AbstractRepeatingCriterion.Conditions {
        public static final Codec<com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions::player),
                Codec.STRING.fieldOf("event").forGetter(com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions::event),
                RacePredicate.CODEC.optionalFieldOf("races", RacePredicate.EMPTY).forGetter(com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions::races),
                CodecUtils.tristateOf("flying").forGetter(com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions::flying),
                Codec.INT.optionalFieldOf("repeatCount", 0).forGetter(com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions::repeatCount)
            ).apply(instance, com.minelittlepony.unicopia.advancement.CustomEventCriterion.Conditions::new));

        public boolean test(int count, ServerPlayer player) {
            boolean isFlying = Pony.of(player).getPhysics().isFlying();
            return races.test(player)
                    && flying.orElse(isFlying) == isFlying
                    && (repeatCount < 0 || repeatCount <= count);
        }
    }
}
