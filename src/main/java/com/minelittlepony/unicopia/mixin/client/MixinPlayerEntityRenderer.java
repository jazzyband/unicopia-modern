package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.client.render.PlayerPoser;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer.FeatureRoot;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

@Mixin(AvatarRenderer.class)
abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {
    MixinPlayerEntityRenderer() { super(null, null, 0); }

    private AvatarRenderState unicopia_createCurrentState() {
        var player = Minecraft.getInstance().player;
        AvatarRenderState state = createRenderState();
        if (player != null) {
            extractRenderState(player, state, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "renderHand", at = @At("RETURN"))
    private void unicopia_renderArm(PoseStack matrices, SubmitNodeCollector vertices, int light,
            Identifier skinTexture, ModelPart arm, boolean sleeveVisible, CallbackInfo info) {
        HumanoidArm side = getModel().leftArm == arm ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        ((FeatureRoot<AvatarRenderState, ?>)this).getAccessories().renderArm(matrices, vertices,
                light, unicopia_createCurrentState(), arm, side);
    }

    @Inject(method = "renderHand", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/UvMapping;)V"))
    private void unicopia_poseArm(PoseStack matrices, SubmitNodeCollector vertices, int light,
            Identifier skinTexture, ModelPart arm, boolean sleeveVisible, CallbackInfo info) {
        PlayerPoser.INSTANCE.applyPosing(matrices, unicopia_createCurrentState(), getModel(),
                arm == getModel().leftArm ? PlayerPoser.Context.FIRST_PERSON_LEFT : PlayerPoser.Context.FIRST_PERSON_RIGHT);
    }
}
