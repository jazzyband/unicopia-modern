package com.minelittlepony.unicopia.mixin.client;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PostPass.class)
abstract class MixinPostPass {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = GpuBuffer.USAGE_UNIFORM))
    private int unicopia$allowUpdatingPostUniforms(int usage) {
        return usage | GpuBuffer.USAGE_COPY_DST;
    }
}
