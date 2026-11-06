package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation.Recipient;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.ExplosionUtil;

/**
 * Kirin ability to transform into a nirik
 */
public class NirikBlastAbility implements Ability<Hit> {
    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 3;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(EquinePredicates.RAGING.test(player.asEntity()));
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
    public boolean apply(Pony player, Hit data) {

        player.asWorld().explode(player.asEntity(), player.damageOf(DamageTypes.FIREBALL), ExplosionUtil.NON_DESTRUCTIVE, player.getOriginVector(), 5, true, ExplosionInteraction.MOB);
        player.setInvulnerabilityTicks(5);

        player.setAnimation(Animation.ARMS_UP, Recipient.ANYONE, 12);
        player.playSound(SoundEvents.POLAR_BEAR_WARNING, 2F, 0.1F);
        player.subtractEnergyCost(25);

        coolDown(player, AbilitySlot.NONE);

        for (Entity e : player.findAllEntitiesInRange(5, EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_CREATIVE_OR_SPECTATOR)).toList()) {
            Vec3 offset = player.getOriginVector().subtract(e.position());
            ((LivingEntity)e).knockback(1, offset.x, offset.z, player.asWorld().damageSources().mobAttack(player.asEntity()), 0);
        }

        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        RandomSource rng = player.asWorld().getRandom();

        for (int i = 0; i < 26; i++) {
            Vec3 pos = player.getOriginVector().add(rng.nextGaussian(), 0, rng.nextGaussian());
            player.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos, new Vec3(rng.nextGaussian(), 0.3, rng.nextGaussian()));
            player.addParticle(ParticleTypes.FLAME, pos, new Vec3(rng.nextGaussian(), 0.8, rng.nextGaussian()));
            player.addParticle(ParticleTypes.LAVA, pos, new Vec3(rng.nextGaussian(), 0.8, rng.nextGaussian()));
        }

    }
}
