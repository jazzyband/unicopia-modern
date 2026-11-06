package com.minelittlepony.unicopia.advancement;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;

public class SendViaDragonBreathScrollCriterion extends AbstractRepeatingCriterion<SendViaDragonBreathScrollCriterion.Conditions> {
    @Override
    public Codec<com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions> codec() {
        return com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions.CODEC;
    }

    public void triggerSent(Player player, ItemStack payload, String recipient, BiConsumer<String, Integer> counterCallback) {
        if (player instanceof ServerPlayer spe) {
            trigger(spe, c -> c.test(spe, payload, recipient, false), (count, c) -> {
                c.counter.ifPresent(counter -> counterCallback.accept(counter, count));
                return true;
            });
        }
    }

    public void triggerReceived(LivingEntity recipient, ItemStack payload) {
        if (recipient instanceof ServerPlayer spe) {
            trigger(spe, c -> c.test(spe, payload, recipient.getDisplayName().getString(), true), (count, c) -> true);
        }
    }

    public record Conditions (
            Optional<Holder<LootItemCondition>> player,
            Optional<ItemPredicate> item,
            boolean isReceivingEnd,
            Optional<String> recipientName,
            TriState recipientPresent,
            Optional<String> counter,
            RacePredicate races
    ) implements AbstractRepeatingCriterion.Conditions {
        public static final Codec<com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions::player),
                ItemPredicate.CODEC.optionalFieldOf("item").forGetter(com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions::item),
                Codec.BOOL.optionalFieldOf("is_receiving_end", false).forGetter(com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions::isReceivingEnd),
                Codec.STRING.optionalFieldOf("recipient_name").forGetter(com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions::recipientName),
                CodecUtils.tristateOf("recipient_present").forGetter(com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions::recipientPresent),
                Codec.STRING.optionalFieldOf("counter").forGetter(com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions::counter),
                RacePredicate.CODEC.optionalFieldOf("races", RacePredicate.EMPTY).forGetter(com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions::races)
            ).apply(instance, com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion.Conditions::new));
        public boolean test(ServerPlayer player, ItemStack payload, String recipient, boolean receiving) {
            return isReceivingEnd == receiving
                    && races.test(player)
                    && (item.isEmpty() || item.get().test(payload))
                    && recipientName.map(expectedRecipientname -> recipient.equalsIgnoreCase(expectedRecipientname)).orElse(true)
                    && (recipientPresent == TriState.DEFAULT || isRecipientAbsent(player.level(), recipient) != recipientPresent.get());
        }

        private boolean isRecipientAbsent(ServerLevel world, String recipient) {
            return world.getEntities(EntityTypeTest.forClass(LivingEntity.class), e -> e.hasCustomName() && e.getCustomName().getString().equalsIgnoreCase(recipient)).isEmpty();
        }
    }
}
