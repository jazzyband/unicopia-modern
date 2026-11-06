package com.minelittlepony.unicopia;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

public interface EquineContext {
    EquineContext ABSENT = () -> Race.UNSET;

    Race getSpecies();

    /**
     * Gets the species this player appears to be.
     * This includes illusions and shape-shifting, and status effects but excludes items that grant abilities without changing their race.
     */
    default Race getObservedSpecies() {
        return getCompositeRace().physical();
    }

    default Race.Composite getCompositeRace() {
        return getSpecies().composite();
    }

    default float getCloudWalkingStrength() {
        return 0;
    }

    default boolean collidesWithClouds() {
        return getCompositeRace().canInteractWithClouds() || getCloudWalkingStrength() >= 1;
    }

    default boolean hasFeatherTouch() {
        return false;
    }

    static EquineContext of(CollisionContext context) {
        if (context == CollisionContext.empty()) {
            return InteractionManager.getInstance().getEquineContext();
        }
        if (context instanceof EntityCollisionContext esc) {
            return of(esc.getEntity());
        }
        return ABSENT;
    }

    static EquineContext of(UseOnContext context) {
        return MoreObjects.firstNonNull(Pony.of(context.getPlayer()), ABSENT);
    }

    static EquineContext of(@Nullable Entity entity) {
        if (entity instanceof EquineContext c) {
            return c;
        }
        return MoreObjects.firstNonNull(Equine.of(entity).orElse(null), ABSENT);
    }
}
