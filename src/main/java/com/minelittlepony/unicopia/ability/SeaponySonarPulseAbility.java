package com.minelittlepony.unicopia.ability;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.UPOIs;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;

public class SeaponySonarPulseAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 100;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.INSTANCE;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 5;
    }

    @Override
    public boolean apply(Pony player, Hit data) {
        player.setAnimation(Animation.ARMS_UP, Animation.Recipient.ANYONE);

        for (Entity target : player.findAllEntitiesInRange(64, e -> {
            return (e instanceof LivingEntity && (e instanceof Monster || isWaterCreature(((LivingEntity)e).getType().getCategory()))) && e.isUnderWater();
        }).sorted(Comparator.comparing(e -> e.distanceTo(player.asEntity()))).toList()) {
            Vec3 offset = target.position().subtract(player.getOriginVector());
            float distance = target.distanceTo(player.asEntity());
            if (distance < 4) {
                float scale = 1 - (distance/10F);
                ((LivingEntity)target).knockback(0.7 * scale, -offset.x, -offset.z, target.damageSources().sonicBoom(player.asEntity()), 0);
                target.hurtServer((ServerLevel)target.level(), target.damageSources().sonicBoom(player.asEntity()), 10 * scale);
            } else {
                emitPing(player, target.position(), 10, 1, 1.3F);
            }
        }
        player.subtractEnergyCost(5);

        if (player.asWorld() instanceof ServerLevel sw) {
            sw.getPoiManager().findClosest(
                    UPOIs::isChest,
                    pos -> player.asWorld().getFluidState(pos).is(FluidTags.WATER) && (player.asWorld().getBlockState(pos).getBlock() instanceof ChestBlock), player.getOrigin(), 64, Occupancy.ANY)
                .ifPresent(chestPos -> {
                    emitPing(player, Vec3.atCenterOf(chestPos), 20, 0.5F, 2F);
                });
        }

        player.playSound(USounds.ENTITY_PLAYER_SEAPONY_SONAR, 1);
        player.spawnParticles(UParticles.SHOCKWAVE, 1);
        player.asEntity().gameEvent(GameEvent.INSTRUMENT_PLAY);

        return true;
    }

    private void emitPing(Pony player, Vec3 pos, int delay, float volume, float pitch) {
        AwaitTickQueue.scheduleTask(player.asWorld(), w -> {
            ParticleUtils.spawnParticle(w, UParticles.SHOCKWAVE, pos, Vec3.ZERO);
            float loudness = Math.max(0, 1.4F - (float)Math.log10(player.getOriginVector().distanceTo(pos)));
            w.playSound(null, pos.x, pos.y, pos.z, USounds.ENTITY_PLAYER_SEAPONY_SONAR, SoundSource.AMBIENT, volume * loudness, pitch);
            w.gameEvent(player.asEntity(), GameEvent.INSTRUMENT_PLAY, pos);
        }, delay + (int)player.getOriginVector().distanceTo(pos));
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }

    private static boolean isWaterCreature(MobCategory group) {
        return switch (group) {
            case AXOLOTLS, UNDERGROUND_WATER_CREATURE, WATER_CREATURE, WATER_AMBIENT -> true;
            default -> false;
        };
    }

}
