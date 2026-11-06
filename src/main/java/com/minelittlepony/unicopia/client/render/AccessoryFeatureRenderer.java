package com.minelittlepony.unicopia.client.render;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;

public class AccessoryFeatureRenderer<
        S extends LivingEntityRenderState,
        M extends EntityModel<? super S>> extends RenderLayer<S, M> {

    private static final List<FeatureFactory<?, ?>> REGISTRY = new ArrayList<>();

    @SafeVarargs
    public static <S extends HumanoidRenderState> void register(FeatureFactory<S, HumanoidModel<S>>...factories) {
        for (var factory : factories) {
            REGISTRY.add(factory);
        }
    }

    private final Iterable<Feature<S>> features;

    @SuppressWarnings("unchecked")
    public AccessoryFeatureRenderer(RenderLayerParent<S, M> context) {
        super(context);
        features = REGISTRY.stream().map(f -> ((FeatureFactory<S, M>)f).create(context)).toList();
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, S state, float limbAngle, float limbDistance) {
        if (MineLPDelegate.getInstance().getRace(state).isEquine()) {
            return;
        }

        if (state instanceof HumanoidRenderState) {
            features.forEach(feature -> feature.render(matrices, vertexConsumers, light, state, limbAngle, limbDistance));
        }

        CasterState caster = CasterState.of(state);
        if (caster != null) {
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertexConsumers, light, caster);
        }
    }

    public void renderArm(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, S entity, ModelPart arm, HumanoidArm side) {
        features.forEach(feature -> feature.renderArm(matrices, vertexConsumers, light, entity, arm, side));
    }

    public boolean beforeRenderArms(ArmRenderer sender, PoseStack matrices, SubmitNodeCollector vertexConsumers, S entity, int light) {
        CasterState caster = CasterState.of(entity);
        if (caster != null) {
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertexConsumers, light, caster);
        }
        boolean cancelled = false;
        for (var feature : features) {
            cancelled |= feature.beforeRenderArms(sender, matrices, vertexConsumers, entity, light);
        }
        return cancelled;
    }

    public interface FeatureFactory<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
        Feature<S> create(RenderLayerParent<S, M> context);
    }

    public interface Feature<S extends LivingEntityRenderState> {
        void render(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, S entity, float limbAngle, float limbDistance);

        default void renderArm(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, S entity, ModelPart arm, HumanoidArm side) {}

        default boolean beforeRenderArms(ArmRenderer sender, PoseStack matrices, SubmitNodeCollector vertexConsumers, S entity, int light) {
            return false;
        }
    }

    public interface FeatureRoot<
            S extends LivingEntityRenderState,
            M extends EntityModel<? super S>> {
        AccessoryFeatureRenderer<S, M> getAccessories();
        @SuppressWarnings("unchecked")
        @Nullable
        static <T extends LivingEntity, M extends EntityModel<? super LivingEntityRenderState>> FeatureRoot<?, M> of(T entity) {
            var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof FeatureRoot) {
                return (FeatureRoot<?, M>)renderer;
            }
            return null;
        }
    }
}
