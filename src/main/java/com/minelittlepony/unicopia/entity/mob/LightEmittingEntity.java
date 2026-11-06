package com.minelittlepony.unicopia.entity.mob;

import com.minelittlepony.unicopia.entity.DynamicLightSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class LightEmittingEntity extends Entity implements DynamicLightSource {
    private final LightEmitter<?> emitter = new LightEmitter<>(this);

    public LightEmittingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        emitter.tick();
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        emitter.remove();
    }
}
