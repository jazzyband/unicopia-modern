package com.minelittlepony.unicopia.mixin.client;

import com.minelittlepony.unicopia.client.render.AnimalPoser;
import com.minelittlepony.unicopia.client.render.PlayerPoser;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelFeatureRenderer.class)
abstract class MixinModelFeatureRenderer {
    @Inject(method = "prepareModel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V",
            shift = At.Shift.AFTER))
    private <S> void unicopia$applyFinalPose(ModelFeatureRenderer.Submit<S> submit, CallbackInfo info) {
        // The submitted transform already includes body movement. Reapply joint poses
        // after the deferred draw resets the model, without applying that transform twice.
        PoseStack unusedTransform = new PoseStack();
        if (submit.state() instanceof AvatarRenderState player && submit.model() instanceof HumanoidModel<?> model) {
            PlayerPoser.INSTANCE.applyPosing(unusedTransform, player, model, PlayerPoser.Context.THIRD_PERSON);
        }
        if (submit.state() instanceof EntityRenderState state && submit.model() instanceof EntityModel<?> model) {
            AnimalPoser.INSTANCE.applyPosing(unusedTransform, state, model);
        }
    }
}
