package com.minelittlepony.unicopia.mixin.client;

import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.model.geom.PartNames;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.ModelPartHooks;
import com.minelittlepony.unicopia.entity.duck.Hookable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

@Mixin(ModelPart.class)
abstract class MixinModelPart implements Hookable {
    @Unique
    private boolean isHeadPart;

    @Shadow
    private boolean visible;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onModelPart(List<Cube> cuboids, Map<String, ModelPart> children, CallbackInfo info) {
        if (((Object)children.getOrDefault(PartNames.HEAD, null)) instanceof Hookable hook) {
            hook.enableHooks();
        }
    }

    @Override
    public void enableHooks() {
        isHeadPart = true;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"))
    public void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color, CallbackInfo info) {
        if (visible && isHeadPart) {
            ModelPartHooks.onHeadRendered((ModelPart)(Object)this, matrices);
        }
    }
}
