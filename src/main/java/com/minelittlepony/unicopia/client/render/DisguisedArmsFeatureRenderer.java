package com.minelittlepony.unicopia.client.render;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;

public class DisguisedArmsFeatureRenderer<S extends HumanoidRenderState> implements AccessoryFeatureRenderer.Feature<S> {

    private final Minecraft client = Minecraft.getInstance();

    private static final Map<EntityType<?>, Identifier> OVERLAY_TEXTURES = Map.of(
        EntityTypes.DROWNED, Identifier.withDefaultNamespace("textures/entity/zombie/drowned_outer_layer.png"),
        EntityTypes.STRAY, Identifier.withDefaultNamespace("textures/entity/skeleton/stray_overlay.png")
    );

    private final Function<EntityType<?>, Set<Pair<ModelPart, ModelPart>>> overlayModelCache = Util.memoize(type -> {
        return ModelLayers.getKnownLocations()
                .filter(layer -> layer.model().equals(EntityType.getKey(type)) && !"main".equals(layer.layer()))
                .map(Minecraft.getInstance().getEntityModels()::bakeLayer)
                .map(model -> {
                    ModelPart arms = getPart(model, PartNames.ARMS).orElse(null);
                    ModelPart leftArm = getPart(model, PartNames.LEFT_ARM)
                            .or(() -> getPart(model, PartNames.LEFT_FRONT_LEG))
                            .orElse(arms);
                    ModelPart rightArm = getPart(model, PartNames.RIGHT_ARM)
                            .or(() -> getPart(model, PartNames.RIGHT_FRONT_LEG))
                            .orElse(arms);
                    return leftArm != null && rightArm != null ? Pair.of(leftArm, rightArm) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    });

    private static Optional<ModelPart> getPart(ModelPart part, String childName) {
        return part.hasChild(childName) ? Optional.of(part.getChild(childName)) : Optional.empty();
    }

    public DisguisedArmsFeatureRenderer(RenderLayerParent<S, ? extends HumanoidModel<S>> context) {

    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, S entity, float limbAngle, float limbDistance) {
    }

    @Override
    public boolean beforeRenderArms(ArmRenderer sender, PoseStack matrices, SubmitNodeCollector vertexConsumers, S entity, int light) {
        if (CasterState.of(entity).appearance instanceof LivingEntity l) {
            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float swingProgress = client.player == null ? 0 : client.player.getSwingAnimation(tickDelta);

            InteractionHand hand = client.player == null || client.player.getCurrentSwing() == null ? InteractionHand.MAIN_HAND : client.player.getCurrentSwing().hand();

            boolean bothHands = l instanceof Zombie || l instanceof IronGolem;

            if (bothHands || hand == InteractionHand.MAIN_HAND) {
                if (client.player != null && client.player.getMainHandItem().isEmpty()) {
                    matrices.pushPose();
                    renderArmHoldingItem(l, matrices, vertexConsumers, light, 1 - sender.getEquipProgress(InteractionHand.MAIN_HAND, tickDelta), hand == InteractionHand.MAIN_HAND ? swingProgress : 0, entity.mainArm);
                    matrices.popPose();
                }
            }

            if (bothHands || hand == InteractionHand.OFF_HAND) {
                if (client.player != null && client.player.getOffhandItem().isEmpty()) {
                    matrices.pushPose();
                    renderArmHoldingItem(l, matrices, vertexConsumers, light, 1 - sender.getEquipProgress(InteractionHand.OFF_HAND, tickDelta), hand == InteractionHand.OFF_HAND ? swingProgress : 0, entity.mainArm.getOpposite());
                    matrices.popPose();
                }
            }
        }

        return false;
    }

    @Nullable
    private ModelPart getArmModel(@Nullable EntityModel<?> model, boolean right) {
        if (model instanceof HumanoidModel bipedModel) {
            return right ? bipedModel.rightArm : bipedModel.leftArm;
        }

        return getOptionalChild(model.root(), PartNames.ARMS).map(arms -> {
            return getOptionalChild(arms, right ? PartNames.RIGHT_ARM : PartNames.LEFT_ARM)
                    .or(() -> getOptionalChild(arms, right ? PartNames.RIGHT_FRONT_LEG : PartNames.LEFT_FRONT_LEG))
                    .orElse(arms);
        }).orElse(null);

    }

    static Optional<ModelPart> getOptionalChild(ModelPart parent, String key) {
        return parent.hasChild(key) ? Optional.of(parent.getChild(key)) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private void renderArmHoldingItem(LivingEntity entity, PoseStack matrices, SubmitNodeCollector vertexConsumers, int light, float equipProgress, float swingProgress, HumanoidArm arm) {
        if (!(client.getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer renderer)) {
            return;
        }

        boolean right = arm != HumanoidArm.LEFT;
        var model = renderer.getModel();
        @Nullable
        ModelPart part = getArmModel(model, right);

        if (part == null) {
            return;
        }

        float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        LivingEntityRenderState state = (LivingEntityRenderState)renderer.createRenderState(entity, tickDelta);

        model.setupAnim(state);

        float signum = right ? 1 : -1;
        float srtSwingProgress = Mth.sqrt(swingProgress);
        float xOffset = -0.3F * Mth.sin(srtSwingProgress * Mth.PI);
        float yOffset = 0.4F * Mth.sin(srtSwingProgress * (Mth.TWO_PI));
        float swingAmount = -0.4F * Mth.sin(swingProgress * Mth.PI);
        matrices.translate(signum * (xOffset + 0.64000005F), yOffset + -0.6F + equipProgress * -0.6F, swingAmount + -0.71999997f);
        matrices.rotate(Axis.YP.rotationDegrees(signum * 45));
        float zRot = Mth.sin(swingProgress * swingProgress * Mth.PI);
        float yRot = Mth.sin(srtSwingProgress * Mth.PI);

        matrices.rotate(Axis.YP.rotationDegrees(signum * yRot * 70));
        matrices.rotate(Axis.ZP.rotationDegrees(signum * zRot * -20));
        matrices.translate(signum * -1, 3.6F, 3.5F);
        matrices.rotate(Axis.ZP.rotationDegrees(signum * 120));
        matrices.rotate(Axis.XP.rotationDegrees(200));
        matrices.rotate(Axis.YP.rotationDegrees(signum * -135));
        matrices.translate(signum * 5.6F, 0, 0);

        if (entity instanceof IronGolem golem) {
            int attackTicks = golem.getAttackAnimationTick();
            if (attackTicks > 0) {
                matrices.rotate(Axis.ZP.rotationDegrees(-signum * part.xRot * Mth.RAD_TO_DEG - 90 * signum));
            }
        }
        part.xRot = 0;

        if (MineLPDelegate.getInstance().getRace(entity).isEquine()) {
            matrices.translate(0, -part.y / 16F, 0);
        }

        Identifier texture = renderer.getTextureLocation(state);
        RenderGeometry.submit(matrices, vertexConsumers, RenderTypes.entityTranslucent(texture),
                (stack, buffer) -> part.render(stack, buffer, light, OverlayTexture.NO_OVERLAY));

        Identifier overlayTexture = OVERLAY_TEXTURES.get(entity.getType());
        if (overlayTexture != null) {
            overlayModelCache.apply(entity.getType()).forEach(arms -> {
                ModelPart armPart = right ? arms.getSecond() : arms.getFirst();
                armPart.loadPose(part.storePose());
                RenderGeometry.submit(matrices, vertexConsumers, RenderTypes.entityTranslucent(overlayTexture),
                        (stack, buffer) -> armPart.render(stack, buffer, light, OverlayTexture.NO_OVERLAY));
            });
        }
    }
}
