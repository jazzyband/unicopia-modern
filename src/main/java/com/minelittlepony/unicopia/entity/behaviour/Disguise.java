package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Optional;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.duck.RotatedView;
import com.minelittlepony.unicopia.entity.player.PlayerDimensions;
import com.minelittlepony.unicopia.entity.player.Pony;

public interface Disguise extends FlightType.Provider, PlayerDimensions.Provider {

    EntityAppearance getAppearance();

    boolean isDead();

    default Optional<EntityAppearance> getOptionalAppearance() {
        return Optional.ofNullable(getAppearance());
    }

    @Override
    default FlightType getFlightType() {
        return getOptionalAppearance().map(EntityAppearance::getFlightType).orElse(FlightType.UNSET);
    }

    @Override
    default Optional<EntityDimensions> getTargetDimensions(Pony player) {
        return getOptionalAppearance().flatMap(d -> d.getTargetDimensions(player));
    }

    default boolean isOf(@Nullable Entity entity) {
        return getAppearance().isOf(entity);
    }

    default Disguise setDisguise(@Nullable Entity entity) {
        if (entity == getAppearance().getEntity()) {
            entity = null;
        }

        getAppearance().setAppearance(entity);
        return this;
    }

    @SuppressWarnings("unchecked")
    default boolean update(Caster<?> caster, boolean tick) {

        if (!(caster instanceof Living<?> source)) {
            return false;
        }

        LivingEntity owner = source.asEntity();

        if (owner == null) {
            return true;
        }

        Entity entity = getAppearance().getOrCreate(source);

        if (entity == null) {
            owner.setInvisible(false);
            if (source instanceof Pony) {
                ((Pony) source).setInvisible(false);
            }

            owner.refreshDimensions();
            return false;
        }

        entity.noPhysics = true;

        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        entity.setInvisible(false);
        entity.setNoGravity(true);

        EntityBehaviour<Entity> behaviour = EntityBehaviour.forEntity(entity);

        behaviour.copyBaseAttributes(owner, entity);

        if (tick && !getAppearance().skipsUpdate()) {
            ((RotatedView)entity.level()).setMirrorEntityStatuses(entity.level().isClientSide());
            if (entity.level().isClientSide()) {
                entity.tick();
            } else {
                entity.tick();
            }

            ((RotatedView)entity.level()).setMirrorEntityStatuses(false);
        }

        if (!(owner instanceof Player) && !((LivingEntityDuck)owner).isJumping()) {
            owner.push(0, -0.09, 0);
        }

        behaviour.update(source, entity, this);

        if (source instanceof Pony player) {
            source.asEntity().setInvisible(true);
            player.setInvisible(true);

            if (entity instanceof Owned.Mutable) {
                ((Owned.Mutable<LivingEntity>)entity).setMaster(player);
            }

            if (entity instanceof Player) {
                entity.getEntityData().set(PlayerAccess.getModelBitFlag(), owner.getEntityData().get(PlayerAccess.getModelBitFlag()));
            }
        }

        return !isDead() && !source.asEntity().isDeadOrDying();
    }

    public static abstract class PlayerAccess extends Player {
        public PlayerAccess() { super(null, null); }
        public static EntityDataAccessor<Byte> getModelBitFlag() {
            return DATA_PLAYER_MODE_CUSTOMISATION;
        }
    }
}
