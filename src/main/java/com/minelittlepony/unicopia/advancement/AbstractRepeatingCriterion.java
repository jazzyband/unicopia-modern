package com.minelittlepony.unicopia.advancement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import com.minelittlepony.unicopia.entity.player.Pony;

public abstract class AbstractRepeatingCriterion<T extends AbstractRepeatingCriterion.Conditions> implements CriterionTrigger<T> {
    protected void trigger(ServerPlayer player, Predicate<T> shouldCount, BiPredicate<Integer, T> predicate) {
        PlayerAdvancements tracker = player.getAdvancements();
        TriggerCountTracker counter = Pony.of(player).getAdvancementProgress();
        counter.removeGranted(player, tracker);

        var advancements = tracker.getTriggerMapForType(this);
        if (advancements != null && !advancements.isEmpty()) {
            LootContext lootContext = EntityPredicate.createContext(player, player);
            List<PlayerAdvancements.TriggerInstanceKey> matches = null;

            for (var condition : advancements.entrySet()) {
                T conditions = condition.getValue();
                var key = condition.getKey();
                if (shouldCount.test(conditions) && predicate.test(counter.update(key.advancement(), key.criterion()), conditions)) {
                    var playerPredicate = conditions.player();
                    if (playerPredicate.isEmpty() || playerPredicate.get().value().test(lootContext)) {
                        if (matches == null) {
                            matches = new ArrayList<>();
                        }

                        matches.add(key);
                    }
                }
            }

            if (matches != null) {
                for (var advancement : matches) {
                    tracker.award(advancement.advancement(), advancement.criterion());
                }
                counter.removeGranted(player, tracker);
            }
        }
    }

    public interface Conditions extends SimpleCriterionTrigger.SimpleInstance {
    }
}
