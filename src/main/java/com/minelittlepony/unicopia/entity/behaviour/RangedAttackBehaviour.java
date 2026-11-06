package com.minelittlepony.unicopia.entity.behaviour;

import java.util.function.BiFunction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.SoundEmitter;

public class RangedAttackBehaviour<T extends Entity & RangedAttackMob> extends EntityBehaviour<T> {

    private final BiFunction<Level, T, Projectile> projectileSupplier;

    private final SoundEvent sound;

    public RangedAttackBehaviour(SoundEvent sound, BiFunction<Level, T, Projectile> projectileSupplier) {
        this.sound = sound;
        this.projectileSupplier = projectileSupplier;
    }

    @Override
    public void update(Pony player, T entity, Disguise spell) {

        if (player.sneakingChanged() && isSneakingOnGround(player)) {

            Projectile spit = projectileSupplier.apply(entity.level(), entity);

            Vec3 rot = player.asEntity().getViewVector(1);

            spit.shoot(rot.x(), rot.y(), rot.z(), 1.5F, 3);
            spit.setOwner(player.asEntity());

            if (!entity.isSilent()) {
                SoundEmitter.playSoundAt(entity, sound, 1, 1 + (entity.level().getRandom().nextFloat() - entity.level().getRandom().nextFloat()) * 0.2F);
            }

            entity.level().addFreshEntity(spit);
        }
    }
}
