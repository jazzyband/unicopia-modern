package com.minelittlepony.unicopia.advancement;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RaceChangeCriterion extends SimpleCriterionTrigger<RaceChangeCriterion.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(Player player) {
        if (player instanceof ServerPlayer) {
            trigger((ServerPlayer)player, c -> c.test((ServerPlayer)player));
        }
    }

    public record Conditions (
            Optional<Holder<LootItemCondition>> player,
            Race race
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Race.REGISTRY.byNameCodec().fieldOf("race").forGetter(Conditions::race)
            ).apply(instance, Conditions::new));

        public boolean test(ServerPlayer player) {
            return Pony.of(player).getCompositeRace().includes(race);
        }
    }
}
