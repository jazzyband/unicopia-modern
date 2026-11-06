package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.ShieldSpell;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;

import static net.minecraft.util.ARGB.*;

public class ShieldSpellRenderer extends SpellRenderer<ShieldSpell, ShieldSpellRenderer.State> {
    private final SphereModel model = new SphereModel(40, 40, DrawableUtil.PI);

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(ShieldSpell spell, State state, Caster<?> caster, float tickDelta) {
        super.updateRenderState(spell, state, caster, tickDelta);
        state.height = caster.asEntity().getEyeY() - caster.getOriginVector().y;
        int typeColor = state.type.type().getColor();
        int ponyColor = MineLPDelegate.getInstance().getMagicColor(caster.getOriginatingCaster().asEntity());

        state.color = ColorHelper.saturate(srgbLerp(
                caster.getCorruption().getScaled(1) * (tickDelta / (1 + caster.asWorld().getRandom().nextFloat())),
                ponyColor == 0 ? typeColor : srgbLerp(0.6F, ponyColor, typeColor),
                0xFF000
        ), 2);
        state.radius = 0.35F + spell.getRadius(tickDelta) + Mth.sin((caster.asEntity().tickCount + tickDelta) / 30F) * 0.01F;
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector vertices, State spell, CasterState caster, int light) {
        super.render(matrices, vertices, spell, caster, light);

        matrices.pushPose();
        matrices.translate(0, spell.height, 0);


        float thickness = 0.02F * Mth.sin(caster.entityState.ageInTicks / 30F);
        float alpha = (1 - Math.abs(Mth.sin(caster.entityState.ageInTicks / 20F)) * 0.1F) * Mth.clamp(spell.radius - 1, 0, 1);

        if (caster.isCamera) {
            matrices.translate(0, -1.75F, 0);
            matrices.rotate(Axis.XP.rotationDegrees(client.getCameraEntity().getViewXRot(client.getDeltaTracker().getGameTimeDeltaPartialTick(false))));
            SpellGeometry.render(matrices, vertices, RenderLayers.getMagicShield(), (stack, buffer) -> model.render(stack, buffer, light, 1, spell.radius, color((int)((alpha * 0.2F) * 255), spell.color)));
        } else {
            matrices.rotate(Axis.XP.rotationDegrees(180));
            matrices.scale(1, spell.radius == 0 ? 1 : Mth.clamp(2.6F / spell.radius, 0.7F, 1.8F), 1);
            SpellGeometry.render(matrices, vertices, RenderLayers.getMagicShield(), (stack, buffer) -> SphereModel.SPHERE.render(stack, buffer, light, 1, spell.radius + thickness, color((int)((alpha * 0.08F) * 255), spell.color)));
            SpellGeometry.render(matrices, vertices, RenderLayers.getMagicShield(), (stack, buffer) -> SphereModel.SPHERE.render(stack, buffer, light, 1, spell.radius - thickness, color((int)((alpha * 0.05F) * 255), spell.color)));
            SpellGeometry.render(matrices, vertices, RenderLayers.getMagicShield(), (stack, buffer) -> SphereModel.SPHERE.render(stack, buffer, light, 1, spell.radius + thickness * 2, color((int)((alpha * 0.05F) * 255), spell.color)));
        }

        matrices.popPose();
    }

    public static class State extends SpellRenderState {
        public double height;
        public int color;
        public float radius;
    }
}
