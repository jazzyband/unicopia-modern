package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.server.world.WeatherAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome.Precipitation;

@Mixin(ClientLevel.class)
abstract class MixinWeatherRendering {
    @ModifyReturnValue(method = "getPrecipitationAt", at = @At("RETURN"))
    private Precipitation unicopia$hidePrecipitationAboveClouds(Precipitation precipitation, BlockPos pos) {
        return ((WeatherAccess)this).isBelowClientCloudLayer(pos) ? precipitation : Precipitation.NONE;
    }
}
