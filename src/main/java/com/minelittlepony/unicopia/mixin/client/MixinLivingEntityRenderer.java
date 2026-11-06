package com.minelittlepony.unicopia.mixin.client;

import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer;
import com.minelittlepony.unicopia.client.render.AnimalPoser;
import com.minelittlepony.unicopia.client.render.PlayerPoser;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer.FeatureRoot;

@Mixin(LivingEntityRenderer.class)
abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S>
        implements RenderLayerParent<S, M>, FeatureRoot<S, M> {
    protected MixinLivingEntityRenderer(Context context) {
        super(context);
    }

    @Shadow
    private @Final List<RenderLayer<S, M>> layers;

    @Nullable
    private AccessoryFeatureRenderer<S, M> accessories;

    @Override
    @SuppressWarnings("unchecked")
    public AccessoryFeatureRenderer<S, M> getAccessories() {
        if (accessories == null) {
            accessories = layers.stream()
                .filter(a -> a instanceof FeatureRoot)
                .map(a -> ((FeatureRoot<S, M>)a).getAccessories())
                .findFirst()
                .orElseGet(() -> {
                    var feature = new AccessoryFeatureRenderer<>(this);
                    layers.add(feature);
                    return feature;
                });
        }
        return accessories;
    }

    @Inject(method = "submit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;isBodyVisible(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z"))
    private void unicopia$transformBodyAndLayers(S state, PoseStack matrices, SubmitNodeCollector vertices,
            CameraRenderState camera, CallbackInfo info) {
        if (state instanceof AvatarRenderState player) {
            PlayerPoser.INSTANCE.applyBodyTransform(matrices, player);
        }
    }

    @Inject(method = "submit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V",
            shift = Shift.AFTER))
    private void onRender(
            S state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera,
            CallbackInfo into) {
        getAccessories();
        if (state instanceof AvatarRenderState player) {
            // The body and all layers already share the submitted body transform.
            // Only update joint poses here, before accessories read the head and limbs.
            PlayerPoser.INSTANCE.applyPosing(new PoseStack(), player, (HumanoidModel<?>)getModel(), PlayerPoser.Context.THIRD_PERSON);
        }

        AnimalPoser.INSTANCE.applyPosing(matrices, state, getModel());
    }

    @Inject(method = "extractRenderState",
            at = @At("TAIL"))
    private void onUpdateRenderState(T entity, S state, float tickDelta, CallbackInfo info) {
        CasterState caster = CasterState.of(state);
        caster.update(Caster.of(entity).orElse(null), tickDelta);
    }
}
