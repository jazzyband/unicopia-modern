package com.minelittlepony.unicopia.entity.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;

public class SimpleStatusEffect extends MobEffect {

    private final boolean instant;

    public SimpleStatusEffect(MobEffectCategory category, int color, boolean instant) {
        super(category, color);
        this.instant = instant;
    }

    protected Holder<MobEffect> getEntry(Entity entity) {
        return entity.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).wrapAsHolder(this);
    }

    @Override
    public final boolean isInstantaneous() {
        return instant;
    }
}
