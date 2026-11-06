package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;

public class LootBugEntity extends Silverfish {
    public LootBugEntity(EntityType<? extends Silverfish> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                getRandomX(1), this.getEyeY(), getRandomZ(1),
                random.nextFloat() - 0.5F, random.nextFloat(), random.nextFloat() - 0.5F
        );
    }
}
