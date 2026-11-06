package com.minelittlepony.unicopia.entity.behaviour;

import java.util.*;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.PlayerAttributes;
import com.minelittlepony.unicopia.util.Swap;

public interface EntitySwap {
    Swap<Entity> POSITION = Swap.of(Entity::position, (entity, pos) -> {
        entity.setPos(pos.x(), pos.y(), pos.z());
        if (entity instanceof PathfinderMob pae && !(entity instanceof Player)) {
            pae.getNavigation().stop();
        }
    });
    Swap<Entity> VELOCITY = Swap.of(Entity::getDeltaMovement, (entity, vel) -> {
        entity.setDeltaMovement(vel);
        Living.updateVelocity(entity);
    });
    Swap<Entity> PITCH = Swap.of(Entity::getXRot, Entity::setXRot);
    Swap<Entity> YAW = Swap.of(Entity::getYRot, Entity::setYRot);
    Swap<Entity> HEAD_YAW = Swap.of(Entity::getYHeadRot, (entity, headYaw) -> {
        entity.setYHeadRot(headYaw);
        if (entity.level() instanceof ServerLevel sw) {
            sw.getChunkSource().sendToTrackingPlayersAndSelf(entity, new ClientboundRotateHeadPacket(entity, (byte)Mth.floor(entity.getYHeadRot() * 256F / 360F)));
        }
    });
    Swap<Entity> BODY_YAW = Swap.of(Entity::getVisualRotationYInDegrees, Entity::setYBodyRot);
    Swap<Entity> FIRE_TICKS = Swap.of(Entity::getRemainingFireTicks, Entity::setRemainingFireTicks);
    Swap<Entity> PASSENGERS = Swap.of(entity -> new ArrayList<>(entity.getPassengers()), (entity, passengers) -> {
       entity.ejectPassengers();
       passengers.forEach(passenger -> passenger.startRiding(entity));
       Living.transmitPassengers(entity);
    });
    Swap<LivingEntity> STATUS_EFFECTS = Swap.of(
            entity -> Set.copyOf(entity.getActiveEffectsMap().values()),
            (entity, effects) -> {
            entity.removeAllEffects();
            effects.forEach(entity::addEffect);
        }
    );
    Swap<LivingEntity> MAX_HEALTH = Swap.of(LivingEntity::getMaxHealth, (e, newMax) -> {
        float oldHealthPercentage = e.getHealth() / e.getMaxHealth();
        e.getAttribute(Attributes.MAX_HEALTH).removeModifier(PlayerAttributes.HEALTH_SWAPPING_MODIFIER_ID);

        float change = newMax - e.getMaxHealth();
        if (!Mth.equal(change, 0)) {
            e.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(PlayerAttributes.healthChange(change));
        }
        e.setHealth(oldHealthPercentage * newMax);
    });
    Swap<LivingEntity> HEALTH = Swap.of(LivingEntity::getHealth, LivingEntity::getMaxHealth, LivingEntity::setHealth, Number::floatValue);
    Swap<LivingEntity> AIR = Swap.of(LivingEntity::getAirSupply, LivingEntity::getMaxAirSupply, LivingEntity::setAirSupply, Number::intValue);

    List<Swap<Entity>> REGISTRY = new ArrayList<>(List.of(
            Swap.union(POSITION, VELOCITY, PITCH, YAW, HEAD_YAW, BODY_YAW, FIRE_TICKS, PASSENGERS),
            Swap.union(STATUS_EFFECTS, MAX_HEALTH, HEALTH, AIR).upcast(e -> e instanceof LivingEntity)
    ));
    Swap<Entity> ALL = Swap.union(REGISTRY);
}
