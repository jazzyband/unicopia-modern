package com.minelittlepony.unicopia.client.render.entity;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.collision.MultiBox;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity.BalloonDesign;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity.BasketType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AirBalloonEntityRenderer extends MobRenderer<AirBalloonEntity, AirBalloonEntityRenderer.State, AirBalloonEntityModel> {
    public AirBalloonEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new AirBalloonEntityModel(AirBalloonEntityModel.getBasketModelData().bakeRoot()), 0);
        addLayer(new BalloonFeature(new AirBalloonEntityModel(AirBalloonEntityModel.getBurnerModelData().bakeRoot()), this,
                i -> i.hasBurner, e -> {
            return getComponentTexture(e.hasSoulFlame ? "soul_burner" : "burner");
        }, (light, entity) -> entity.isAscending ? 0xFF00FF : light));
        addLayer(new BalloonFeature(new AirBalloonEntityModel(AirBalloonEntityModel.getCanopyModelData().bakeRoot()), this,
                i -> i.hasBalloon,
                e -> getComponentTexture("canopy/" + e.design.getSerializedName()),
                (light, entity) -> entity.hasBurner && entity.isAscending ? light | 0x00005F : light)
        );
        addLayer(new BalloonFeature(new AirBalloonEntityModel(AirBalloonEntityModel.getSandbagsModelData().bakeRoot()),
                this, e -> e.hasBalloon && e.inflation >= 1, e -> getComponentTexture("sandbags"),
                (light, entity) -> entity.hasBurner && entity.isAscending ? light | 0x00003F : light));
    }

    @Override
    public void submit(State entity, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        matrices.pushPose();
        if (entity.hasRedOverlay) {
            matrices.rotate(Axis.XP.rotationDegrees(Mth.sin(entity.ageInTicks) * 3));
        }
        super.submit(entity, matrices, vertices, camera);
        matrices.popPose();

    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AirBalloonEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.yVelocity = Mth.clamp((float)Math.abs(entity.getDeltaMovement().y()) * 3F, 0.25F, 1F);
        state.yaw = entity.getDirection().toYRot();
        state.design = entity.getDesign();
        state.basket = entity.getBasketType();
        state.inflation = entity.getInflation(tickDelta);
        state.hasBurner = entity.hasBurner();
        state.hasBalloon = entity.hasBalloon();
        state.isAscending = entity.isAscending();
        state.boundingBox = entity.getBoundingBox();
        state.hasSoulFlame = entity.getItemInHand(InteractionHand.MAIN_HAND).is(Items.SOUL_LANTERN);
        state.pos = entity.position();
        state.burnerWiggleProgress = entity.getBurner().getPullProgress(tickDelta);
        state.basketRoll = Mth.clamp(entity.getXVelocity(tickDelta), -0.5F, 0.5F);
        state.basketPitch = Mth.clamp(entity.getZVelocity(tickDelta), -0.5F, 0.5F);
        state.accessoriesPivotX = state.inflation * Mth.sin(state.walkAnimationSpeed + state.ageInTicks / 5F) / 4F;
        state.burnerPivoyY = 32 * (1 - state.inflation) - (9 * state.inflation);
        state.sandbagsPitch = Mth.cos(state.walkAnimationSpeed + state.ageInTicks / 5F) / 80F;
        state.sandbagsRoll = Mth.sin(state.walkAnimationSpeed + state.ageInTicks / 5F) / 80F;
        if (!state.leashStates.isEmpty()) {
            state.basketRoll *= -1;
            state.basketPitch *= -1;
        }
        for (int i = 0; i < state.sandbagPullAmounts.length; i++) {
            state.sandbagPullAmounts[i] = entity.getSandbag(i).getPullProgress(tickDelta);
        }

    }

    public static class State extends LivingEntityRenderState {
        public BalloonDesign design;
        public BasketType basket;

        public float yVelocity;

        public float inflation;
        public float burnerWiggleProgress;
        public float burnerPivoyY;

        public float basketPitch;
        public float basketRoll;

        public float accessoriesPivotX;

        public float sandbagsPitch;
        public float sandbagsRoll;

        public boolean hasBurner;
        public boolean hasBalloon;
        public boolean isAscending;
        public boolean hasSoulFlame;
        public AABB boundingBox;
        public Vec3 pos;

        public float yaw;

        public float[] sandbagPullAmounts = new float[4];
    }

    @Override
    protected AABB getBoundingBoxForCulling(AirBalloonEntity entity, float tickDelta) {
        if (entity.hasBalloon()) {
            return entity.getBalloonBoundingBox(entity.getBoundingBox()).setMinY(entity.getY());
        }
        return entity.getInteriorBoundingBox(entity.getBoundingBox());
    }

    @Override
    public Identifier getTextureLocation(State entity) {
        return getComponentTexture("basket/" + entity.basket.id().getPath());
    }

    @Override
    protected float getFlipDegrees() {
        return 0;
    }

    private Identifier getComponentTexture(String componentName) {
        return Unicopia.id("textures/entity/air_balloon/" + componentName + ".png");
    }

    final class BalloonFeature extends RenderLayer<State, AirBalloonEntityModel> {
        private final AirBalloonEntityModel model;
        private final Predicate<State> visibilityTest;
        private final Function<State, Identifier> textureFunc;
        private final BiFunction<Integer, State, Integer> lightFunc;

        public BalloonFeature(AirBalloonEntityModel model,
                RenderLayerParent<State, AirBalloonEntityModel> context,
                Predicate<State> visibilityTest,
                Function<State, Identifier> textureFunc,
                BiFunction<Integer, State, Integer> lightFunc) {
            super(context);
            this.model = model;
            this.visibilityTest = visibilityTest;
            this.textureFunc = textureFunc;
            this.lightFunc = lightFunc;
        }

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector vertices, int light, State state, float limbDistance, float limbAngle) {
            if (visibilityTest.test(state)) {
                coloredCutoutModelCopyLayerRender(model, textureFunc.apply(state), matrices, vertices, lightFunc.apply(light, state), state, CommonColors.WHITE, 0);
            }
        }
    }
}
