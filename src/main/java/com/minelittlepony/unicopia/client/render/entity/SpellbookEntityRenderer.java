package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.SpellbookEntity;
import com.minelittlepony.unicopia.server.world.Altar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Pose;

public class SpellbookEntityRenderer extends LivingEntityRenderer<SpellbookEntity, SpellbookEntityRenderer.State, SpellbookModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/spellbook/normal.png");
    private static final Identifier ALTAR_BEAM_TEXTURE = Identifier.withDefaultNamespace("textures/entity/end_crystal/end_crystal_beam.png");

    public SpellbookEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SpellbookModel(SpellbookModel.getTexturedModelData().bakeRoot()), 0);
        addLayer(new AltarBeamFeature(this));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SpellbookEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.floatPosition = Mth.sin((entity.tickCount + entity.getId()) / 20) * 0.04F;
        state.hasBeams = entity.hasBeams();
        state.altar = entity.getAltar().orElse(null);

        if (state.open) {
            state.breath = Mth.sin(entity.tickCount / 20) * 0.01F + 0.1F;

            state.leftPageRot = Math.min(state.walkAnimationPos + (state.breath * 10), 1);
            state.rightPageRot = Math.min(1 - state.leftPageRot, 1);
            state.openAngle = 0.9f - state.walkAnimationSpeed;

            state.leftPageRot = state.ageInTicks % 250 < 5 ? (state.ageInTicks % 5) / 5F : state.leftPageRot;
            state.rightPageRot = state.ageInTicks % 250 > 105 && state.ageInTicks % 250 < 110  ? 1-(state.ageInTicks % 5) / 5F : state.rightPageRot;
        } else {
            state.leftPageRot = 0;
            state.rightPageRot = 0;
            state.openAngle = 0;
            state.breath = 0;
        }
    }

    @Override
    public Identifier getTextureLocation(State entity) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0;
    }

    @Override
    protected void setupRotations(State state, PoseStack matrices, float animationProgress, float bodyYaw) {
        super.setupRotations(state, matrices, animationProgress, bodyYaw + 90);

        if (state.open) {
            matrices.translate(-1.25F, -0.35F, 0);
            matrices.translate(0, state.floatPosition, 0);
            matrices.rotate(Axis.ZN.rotationDegrees(60));
        } else {
            matrices.translate(-1.5F, 0.1F, 0.2F);
            matrices.rotate(Axis.ZN.rotationDegrees(90));
            matrices.rotate(Axis.YN.rotationDegrees(90));
        }
    }

    @Override
    protected boolean shouldShowName(SpellbookEntity targetEntity, double distance) {
        return super.shouldShowName(targetEntity, distance)
                && (targetEntity.isCustomNameVisible()
                        || targetEntity.hasCustomName()
                        && targetEntity == entityRenderDispatcher.crosshairPickEntity);
    }

    static class AltarBeamFeature extends RenderLayer<SpellbookEntityRenderer.State, SpellbookModel> {
        public AltarBeamFeature(RenderLayerParent<SpellbookEntityRenderer.State, SpellbookModel> context) {
            super(context);
        }

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector vertices, int light, SpellbookEntityRenderer.State state, float limbPos, float limbSpeed) {
            if (!state.hasBeams) {
                return;
            }

            matrices.last();
            matrices.popPose();
            matrices.pushPose();


            Vec3 center = Vec3.atCenterOf(state.altar.origin()).add(0, -1, 0);

            Vec3 bookPos = new Vec3(state.x, state.y, state.z);
            Vec3 shift = bookPos.subtract(center);

            matrices.pushPose();
            matrices.rotate(Axis.ZP.rotationDegrees(180));
            matrices.translate(shift.x, shift.y - 1, shift.z);

            for (BlockPos pillar : state.altar.pillars()) {
                renderBeam(center.subtract(Vec3.atCenterOf(pillar)), state.ageInTicks, matrices, vertices, light, 1, 0, 1);
            }

            matrices.popPose();
        }

        public static float getYOffset(float animationProgress) {
            animationProgress = Mth.sin(animationProgress * 0.2F) * 0.5F + 0.5F;
            return ((animationProgress * animationProgress + animationProgress) * 0.4F) - 1.4F;
        }
    }

    public static void renderBeam(Vec3 offset, float age, PoseStack matrices, SubmitNodeCollector buffers, int light, float r, float g, float b) {
        final float horizontalDistance = (float)offset.horizontalDistance();
        final float distance = (float)offset.length();
        matrices.pushPose();
        matrices.rotate(Axis.YP.rotation((float)(-Math.atan2(offset.z, offset.x)) - 1.5707964f));
        matrices.rotate(Axis.XP.rotation((float)(-Math.atan2(horizontalDistance, offset.y)) - 1.5707964f));
        final float minV = age * 0.01f;
        final float maxV = minV + (distance / 32F);
        final int sides = 8;
        final float diameter = 0.35F;
        buffers.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(ALTAR_BEAM_TEXTURE), (entry, buffer) -> {
        float segmentX = 0;
        float segmentY = diameter;
        float minU = 0;
        Matrix4f positionMat = entry.pose();

        for (int i = 1; i <= sides; i++) {
            float o = Mth.sin(i * Mth.TWO_PI / sides) * diameter;
            float p = Mth.cos(i * Mth.TWO_PI / sides) * diameter;
            float maxU = i / (float)sides;
            buffer.addVertex(positionMat, segmentX * 0.2F, segmentY * 0.2F, 0).setColor(0, 0, 0, 255).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(entry, 0, -1, 0);
            buffer.addVertex(positionMat, segmentX, segmentY, distance).setColor(r, g, b, 1).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(entry, 0, -1, 0);
            buffer.addVertex(positionMat, o, p, distance).setColor(r, g, b, 1).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(entry, 0, -1, 0);
            buffer.addVertex(positionMat, o * 0.2F, p * 0.2F, 0).setColor(0, 0, 0, 255).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(entry, 0, -1, 0);
            segmentX = o;
            segmentY = p;
            minU = maxU;
        }
        });
        matrices.popPose();
    }

    public static class State extends LivingEntityRenderState {
        public boolean open;
        public boolean hasBeams;
        public float floatPosition;

        @Nullable
        public Altar altar;

        public float breath;

        public float leftPageRot;
        public float rightPageRot;
        public float openAngle;
    }
}