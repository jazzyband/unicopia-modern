package com.minelittlepony.unicopia.block.zap;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;

public interface ElectrifiedBlock {

    default void spawnElectricalParticles(Level world, BlockPos pos, RandomSource random) {
        world.addParticle(new LightningBoltParticleEffect(true, 10, 1, 0.6F, Optional.empty()),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                0, 0, 0
        );
    }

    default float getBlockBreakingDelta(float delta, Player player) {
        if (Pony.of(player).getCompositeRace().canUseEarth()) {
            delta *= 50;
        }

        return Mth.clamp(delta, 0, 0.9F);
    }

    default void triggerLightning(BlockState state, Level world, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        if (world instanceof ServerLevel serverWorld) {
            LightningBolt lightning = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.EVENT);
            world.getEntities(null, AABB.unitCubeFromLowerCorner(center).inflate(7)).forEach(entity -> {
                shockEntity(serverWorld, center, lightning, entity);
            });
        }
        world.gameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Context.of(state));
        ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, center, Vec3.ZERO);
    }

    default void triggerLightning(BlockState state, Level world, BlockPos pos, LivingEntity entity, boolean knockBack) {
        Vec3 center = Vec3.atCenterOf(pos);
        if (world instanceof ServerLevel serverWorld) {
            shockEntity(serverWorld, center, EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.EVENT), entity);
        }
        if (knockBack) {
            Vec3 offset = center.subtract(entity.position());
            entity.knockback(0.8, offset.x, offset.z, world.damageSources().lightningBolt(), 1);
        }
        world.gameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Context.of(state));
        ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, center, Vec3.ZERO);
    }

    private static void shockEntity(ServerLevel serverWorld, Vec3 center, LightningBolt lightning, Entity entity) {
        if (entity instanceof ItemEntity) {
            return;
        }
        float dist = (float)entity.position().distanceTo(center);
        if (dist < 4) {
            entity.thunderHit(serverWorld, EntityTypes.LIGHTNING_BOLT.create(serverWorld, EntitySpawnReason.EVENT));
        } else {
            float damage = 3 / dist;
            if (damage > 1) {
                entity.hurtServer(serverWorld, entity.damageSources().lightningBolt(), damage);
            }
        }
    }
}
