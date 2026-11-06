package com.minelittlepony.unicopia.projectile;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.mixin.MixinPersistentProjectileEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public interface ProjectileUtil {

    /**
     * Checks if the given entity is a projectile.
     */
    static boolean isProjectile(Entity e) {
        return e instanceof Projectile;
    }

    /**
     * Checks if the given entity is a projectile that is not stuck in the ground.
     */
    static boolean isFlyingProjectile(Entity e) {
        return isProjectile(e) && !(e instanceof MixinPersistentProjectileEntity m && m.callIsInGround());
    }

    /**
     * Checks if the given projectile was thrown by the given entity
     */
    static <T extends Entity> boolean isProjectileThrownBy(Entity throwable, @Nullable T e) {
        return e != null && isProjectile(throwable) && e.equals(((Projectile) throwable).getOwner());
    }

    /**
     * Sets the velocity and heading for a projectile.
     *
     * @param throwable     The projectile
     * @param heading       The directional heaving vector
     * @param velocity      Velocity
     * @param inaccuracy    Inaccuracy
     * @return              True the projectile's heading was set, false otherwise
     */
    static void setThrowableHeading(Entity throwable, Vec3 heading, float velocity, float inaccuracy) {

        if (throwable instanceof Projectile p) {
            p.shoot(heading.x, heading.y, heading.z, velocity, inaccuracy);
        } else {
            heading = heading.normalize().scale(velocity);

            Vec3 vel = throwable.getDeltaMovement();

            throwable.push(heading.x - vel.x, heading.y - vel.y, heading.z - vel.z);
            Living.updateVelocity(throwable);
        }
    }

    /**
     * Reverses a projectile's direction to deflect it off a surface.
     *
     * @param projectile The projectile to deflect
     * @param post       The position the projectile must be deflected away from
     * @param absorbtionRate Percentage of the projectile velocity to absorb.
     */
    static void ricochet(Entity projectile, Vec3 pos, float absorbtionRate) {
        Vec3 position = projectile.position();
        Vec3 motion = projectile.getDeltaMovement();

        Vec3 normal = position.subtract(pos).normalize();
        Vec3 approach = motion.subtract(normal);

        if (approach.length() < motion.length()) {
            normal = normal.scale(-1);
        }

        setThrowableHeading(projectile, normal, (float)motion.length() * absorbtionRate, 0);
    }
}
