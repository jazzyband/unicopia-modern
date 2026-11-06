package com.minelittlepony.unicopia.mixin.forgified;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Pseudo
@Mixin(targets = "net.neoforged.neoforge.common.extensions.IAbstractBoatExtension")
interface MixinIForgeBoat {
    @ModifyVariable(
            method = "canBoatInFluid(Lnet/minecraft/world/level/material/FluidState;)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true,
            require = 0
    )
    private FluidState modifyFluidState(FluidState incoming) {
        if (this instanceof LavaAffine a && a.isLavaAffine()) {
            if (incoming.is(FluidTags.WATER)) {
                return Fluids.LAVA.defaultFluidState();
            }
            if (incoming.is(FluidTags.LAVA)) {
                return Fluids.WATER.defaultFluidState();
            }
        }

        return incoming;
    }
    @SuppressWarnings("deprecation")
    @ModifyVariable(
            method = "canBoatInFluid(Lnet/minecraft/world/level/material/Fluid;)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true,
            require = 0
    )
    private Fluid modifyFluid(Fluid incoming) {
        if (this instanceof LavaAffine a && a.isLavaAffine()) {
            if (incoming.is(FluidTags.WATER)) {
                return Fluids.LAVA;
            }
            if (incoming.is(FluidTags.LAVA)) {
                return Fluids.WATER;
            }
        }

        return incoming;
    }
}
