package com.minelittlepony.unicopia.mixin;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.duck.RotatedView;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import com.minelittlepony.unicopia.server.world.WeatherAccess;

@Mixin(Level.class)
abstract class MixinWorld implements LevelAccessor, BlockDestructionManager.Source, RotatedView, WeatherAccess {
    private final Supplier<BlockDestructionManager> destructions = BlockDestructionManager.create((Level)(Object)this);

    @Nullable
    private Float rainGradientOverride;
    @Nullable
    private Float thunderGradientOverride;

    private boolean mirrorEntityStatuses;

    @Override
    public void setMirrorEntityStatuses(boolean enable) {
        mirrorEntityStatuses = enable;
    }

    @Override
    public BlockDestructionManager getDestructionManager() {
        return destructions.get();
    }

    @Override
    public void setWeatherOverride(Float rain, Float thunder) {
        rainGradientOverride = rain;
        thunderGradientOverride = thunder;
    }

    @Inject(method = "broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V", at = @At("HEAD"))
    private void onSendEntityStatus(Entity entity, byte status, CallbackInfo info) {
        if (mirrorEntityStatuses) {
            entity.handleEntityEvent(status);
        }
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void onGetThunderGradient(float delta, CallbackInfoReturnable<Float> info) {
        if (thunderGradientOverride != null) {
            info.setReturnValue(thunderGradientOverride * ((Level)(Object)this).getRainLevel(delta));
        }
    }

    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void onGetRainGradient(float delta, CallbackInfoReturnable<Float> info) {
        if (rainGradientOverride != null) {
            info.setReturnValue(rainGradientOverride);
        }
    }

    @ModifyReturnValue(method = "isRainingAt", at = @At("RETURN"))
    private boolean onHasRain(boolean hasRain, BlockPos pos) {
        return (hasRain && isBelowCloudLayer(pos)) || isInRangeOfStorm(pos);
    }
}

