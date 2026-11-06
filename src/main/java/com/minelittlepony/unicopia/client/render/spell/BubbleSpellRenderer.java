package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.BubbleSpell;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;

public class BubbleSpellRenderer extends SpellRenderer<BubbleSpell, BubbleSpellRenderer.State> {
    static final int BUBBLE_COLOR = ARGB.colorFromFloat(0.9F, 0.9F, 1, 0.25F);
    static final int BUBBLE_SHINE_COLOR = ARGB.colorFromFloat(0.9F, 0.9F, 1, 0.3F);

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(BubbleSpell spell, State state, Caster<?> caster, float tickDelta) {
        super.updateRenderState(spell, state, caster, tickDelta);
        state.height = caster.asEntity().getEyeY() - caster.getOriginVector().y();
        state.radius = spell.getRadius(tickDelta) * 1.5F;
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector vertices, State state, CasterState caster, int light) {
        super.render(matrices, vertices, state, caster, light);

        matrices.pushPose();

        matrices.translate(0, state.radius * 0.5F + state.height, 0);


        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        matrices.pushPose();
        matrices.rotate(Axis.XP.rotationDegrees(90));
        matrices.rotate(Axis.ZN.rotationDegrees(cameraEntity.getViewYRot(tickDelta) - 25));
        matrices.rotate(Axis.YP.rotationDegrees(-cameraEntity.getViewXRot(tickDelta)));

        SpellGeometry.render(matrices, vertices, RenderLayers.getMagicShield(), (stack, buffer) -> new SphereModel(40, 40, DrawableUtil.PI * 0.25F).render(stack, buffer, light, 0, state.radius - 0.1F, BUBBLE_SHINE_COLOR));
        matrices.popPose();

        SpellGeometry.render(matrices, vertices, RenderLayers.getMagicShield(), (stack, buffer) -> SphereModel.SPHERE.render(stack, buffer, light, 0, state.radius, BUBBLE_COLOR));

        matrices.popPose();
    }

    static class State extends SpellRenderState {
        public double height;
        public float radius;
    }
}
