package com.minelittlepony.unicopia.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.SwingState.class)
public interface SwingStateAccessor {
    @Accessor("currentSwing")
    LivingEntity.SwingDescription unicopia$getCurrentSwing();

    @Accessor("currentSwing")
    void unicopia$setCurrentSwing(LivingEntity.SwingDescription swing);

    @Accessor("ticks")
    int unicopia$getTicks();

    @Accessor("ticks")
    void unicopia$setTicks(int ticks);

    @Accessor("oldAnimation")
    float unicopia$getOldAnimation();

    @Accessor("oldAnimation")
    void unicopia$setOldAnimation(float animation);

    @Accessor("animation")
    float unicopia$getAnimation();

    @Accessor("animation")
    void unicopia$setAnimation(float animation);
}
