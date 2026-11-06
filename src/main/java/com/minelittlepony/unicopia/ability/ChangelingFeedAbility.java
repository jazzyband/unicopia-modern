package com.minelittlepony.unicopia.ability;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.ChangelingFeedingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

/**
 * Changeling ability to restore health from mobs
 */
public class ChangelingFeedAbility implements Ability<Hit> {
    private static final Predicate<Entity> TARGET_PREDICATE = e -> (e instanceof LivingEntity)
            && (e instanceof Cow
            || e instanceof AbstractVillager
            || e instanceof Player
            || e instanceof Sheep
            || e instanceof Pig
            || e instanceof Monster);

    @Override
    public int getWarmupTime(Pony player) {
        return 5;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return !SpellType.FEED.isOn(player) && ChangelingFeedingSpell.canFeed(player) ? 15 : 80;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(ChangelingFeedingSpell.canFeed(player) && !getTargets(player).findAny().isEmpty());
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean apply(Pony iplayer, Hit data) {
        if (!ChangelingFeedingSpell.canFeed(iplayer)) {
            return false;
        }

        Player player = iplayer.asEntity();

        float maximumHealthGain = player.getMaxHealth() - player.getHealth();
        int maximumFoodGain = player.canEat(false) ? (20 - player.getFoodData().getFoodLevel()) : 0;

        if (maximumHealthGain > 0 || maximumFoodGain > 0) {
            List<LivingEntity> targets = getTargets(iplayer).map(LivingEntity.class::cast).toList();

            if (targets.size() > 0) {
                new ChangelingFeedingSpell(targets, maximumHealthGain, maximumFoodGain).apply(iplayer);

                iplayer.playSound(USounds.ENTITY_PLAYER_CHANGELING_FEED.value(), 0.1F, iplayer.getRandomPitch());
                return true;
            }
        }

        iplayer.playSound(USounds.Vanilla.PLAYER_BURP, 1, player.level().getRandom().triangle(1F, 0.2F));
        return true;
    }

    protected Stream<Entity> getTargets(Pony player) {
        return Stream.concat(
                VecHelper.findInRange(player.asEntity(), player.asWorld(), player.getOriginVector(), 3, TARGET_PREDICATE).stream(),
                TraceHelper.findEntity(player.asEntity(), 17, 1, TARGET_PREDICATE).stream()
        ).distinct();
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        if (player.asWorld().getRandom().nextInt(10) == 0) {
            player.spawnParticles(ParticleTypes.HEART, 1);
        }
    }
}
