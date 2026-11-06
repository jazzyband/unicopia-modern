package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.ability.magic.spell.effect.HydrophobicSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

@Mixin(FlowingFluid.class)
abstract class MixinFlowableFluid {
    @ModifyReturnValue(method = "canHoldFluid(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/Fluid;)Z", at = @At("RETURN"))
    private static boolean onCanFill(boolean upstreamCheck, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return upstreamCheck && !HydrophobicSpell.blocksFluidFlow(world, pos, fluid.defaultFluidState());
    }
}
