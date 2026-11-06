package com.minelittlepony.unicopia.mixin;

import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Shulker.class)
public interface MixinShulkerEntity {
    @Accessor
    float getCurrentPeekAmount();

    @Accessor
    void setCurrentPeekAmount(float value);

    @Accessor("currentPeekAmountO")
    float getPrevOpenProgress();

    @Accessor("currentPeekAmountO")
    void setPrevOpenProgress(float value);

    @Invoker("getRawPeekAmount")
    int callGetPeekAmount();

    @Invoker("setRawPeekAmount")
    void callSetPeekAmount(int amount);
}
