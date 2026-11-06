package com.minelittlepony.unicopia.advancement;

import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class TraitDiscoveredCriterion extends SimpleCriterionTrigger<TraitDiscoveredCriterion.Conditions> {
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public static Criterion<?> create(Set<Trait> traits) {
        return UCriteria.TRAIT_DISCOVERED.createCriterion(new Conditions(Optional.empty(), traits));
    }

    public void trigger(Player player) {
        if (player instanceof ServerPlayer) {
            trigger((ServerPlayer)player, c -> c.test((ServerPlayer)player));
        }
    }

    public record Conditions(Optional<Holder<LootItemCondition>> player, Set<Trait> traits)  implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Trait.SET_CODEC.fieldOf("traits").forGetter(Conditions::traits)
            ).apply(instance, Conditions::new));

        public boolean test(ServerPlayer player) {
            return Pony.of(player).getDiscoveries().isKnown(traits);
        }
    }
}
