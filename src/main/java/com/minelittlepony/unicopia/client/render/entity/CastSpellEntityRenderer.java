package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.PlaneModel;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class CastSpellEntityRenderer extends EntityRenderer<CastSpellEntity, CastSpellEntityRenderer.State> {
    private static final Identifier[] TEXTURES = new Identifier[] {
            Unicopia.id("textures/particles/runes_0.png"),
            Unicopia.id("textures/particles/runes_1.png"),
            Unicopia.id("textures/particles/runes_2.png"),
            Unicopia.id("textures/particles/runes_3.png"),
            Unicopia.id("textures/particles/runes_4.png"),
            Unicopia.id("textures/particles/runes_5.png")
    };

    public CastSpellEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CastSpellEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.angle = (state.ageInTicks / 9F) % 360;
        state.boundingBoxHeight = entity.getBbHeight();
        state.scale = entity.getScale(tickDelta);
        state.pitch = entity.getViewXRot(tickDelta);
        state.yaw = entity.getViewYRot(tickDelta);
        state.yOffset = (-state.pitch / 90F) * state.boundingBoxHeight * 0.5F;
        state.state.update(entity, tickDelta);
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        Minecraft client = Minecraft.getInstance();
        if (client.getCameraEntity() instanceof CastSpellEntity) {
            return;
        }
        matrices.pushPose();
        matrices.translate(0, 0.001 + state.yOffset, 0);
        matrices.rotate(Axis.YP.rotationDegrees(state.yaw));
        matrices.rotate(Axis.XP.rotationDegrees(-state.pitch));

        SpellRenderState spellState = state.state.spells.isEmpty() ? null : state.state.spells.getFirst();
        if (spellState != null) {
            renderAmbientEffects(matrices, vertices, state, spellState, state.lightCoords);
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertices, state.lightCoords, state.state, spellState);
        }
        matrices.popPose();
    }

    protected void renderAmbientEffects(PoseStack matrices, SubmitNodeCollector vertices, State state, SpellRenderState spell, int light) {
        matrices.pushPose();
        matrices.rotate(Axis.XP.rotationDegrees(90));

        float scale = state.scale * 3;
        matrices.scale(scale, scale, scale);

        @Nullable
        SpellRenderer<?, ?> renderer = spell == null ? null : SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell);

        for (int i = 0; i < TEXTURES.length; i++) {
            if (renderer != null && !renderer.shouldRenderEffectPass(i)) {
                continue;
            }

            final Identifier texture = TEXTURES[i];
            for (int dim = 0; dim < 3; dim++) {
                final int ringIndex = dim;
                float ringSpeed = (i % 2 == 0 ? i : -1) * i;

                matrices.pushPose();
                matrices.rotate(Axis.ZP.rotationDegrees(state.angle * ringSpeed));
                matrices.rotate(Axis.YP.rotationDegrees(state.angle * ringSpeed * dim));
                matrices.rotate(Axis.XP.rotationDegrees(state.angle * ringSpeed * dim));
                vertices.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(texture), (pose, buffer) -> {
                    PoseStack stack = new PoseStack();
                    stack.mulPose(pose.pose());
                    PlaneModel.INSTANCE.render(stack, buffer, light, OverlayTexture.NO_OVERLAY, 1,
                            ARGB.color(Mth.clamp((int)(255 * (scale / ((float)(ringIndex * 3) + 1))), 0, 255), spell.type.type().getColor()));
                });
                matrices.popPose();
            }
        }

        matrices.popPose();
    }

    public static class State extends EntityRenderState {
        public float scale;
        public float pitch;
        public float yaw;

        public float yOffset;

        public float angle;

        public CasterState state = new CasterState(this);
    }
}

