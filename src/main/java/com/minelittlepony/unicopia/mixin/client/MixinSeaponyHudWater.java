package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import com.minelittlepony.unicopia.client.gui.HudEffects;

@Mixin(Entity.class)
abstract class MixinSeaponyHudWater {
    @ModifyReturnValue(method = "isEyeInFluid", at = @At("RETURN"))
    private boolean unicopia_invertWaterForHud(boolean original, TagKey<Fluid> fluid) {
        return ((Object)this instanceof Player player)
                && fluid == net.minecraft.tags.FluidTags.WATER
                && HudEffects.shouldInvertWater(player) ? !original : original;
    }
}
