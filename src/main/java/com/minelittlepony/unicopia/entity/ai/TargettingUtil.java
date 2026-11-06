package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.entity.player.Pony;

public interface TargettingUtil {
    Comparator<Player> FLYING_PREFERRED = Comparator.comparing(e -> Pony.of(e).getPhysics().isFlying() ? 0 : 1);

    @SuppressWarnings("unchecked")
    static <T extends LivingEntity> Stream<T> getTargets(Class<T> type, TargetingConditions predicate, LivingEntity subject, AABB searchArea) {
        if (type == Player.class || type == ServerPlayer.class) {
            return (Stream<T>)((ServerLevel)subject.level()).getNearbyPlayers(predicate, subject, searchArea).stream();
        }
        return ((ServerLevel)subject.level()).getNearbyEntities(type, predicate, subject, searchArea).stream();
    }

    static <T extends Entity> Comparator<T> nearestTo(LivingEntity subject) {
        Vec3 fromPos = subject.getEyePosition();
        return Comparator.comparing(e -> fromPos.distanceTo(e.position()));
    }

    static Vec3 getProjectedPos(LivingEntity entity) {
        if (entity instanceof Player player) {
            Vec3 velocity = Pony.of(player).getPhysics().getTrackedVelocity();
            return entity.getEyePosition().add(velocity.scale(1.5)).add(0, -1, 0);
        }
        return entity.getEyePosition().add(entity.getDeltaMovement());
    }
}
