package com.minelittlepony.unicopia.client.render.spell;

import java.util.UUID;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.PortalSpell;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.EntityReference.EntityValues;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class PortalSpellRenderer extends SpellRenderer<PortalSpell, PortalSpellRenderer.State> {
    @Override
    public boolean shouldRenderEffectPass(int pass) {
        return pass == 0;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(PortalSpell spell, State state, Caster<?> caster, float tickDelta) {
        super.updateRenderState(spell, state, caster, tickDelta);
        state.destination.copyFrom(spell.getDestinationReference());
        state.shouldRenderContents = true;
        state.yaw = spell.getYaw();
        state.portalUuid = caster.asEntity().getUUID();
        state.portalState = new EntityReference.EntityValues<>(caster.asEntity());
        state.orientationChange = spell.getOrientationChange();
        state.positionMatrix = spell.getPositionMatrix(caster, state.portalState.pos(), state.orientationChange, new Matrix4f());
        state.pitchChange = -spell.getTargetPitch() + spell.getPitch();
        state.yawChange = spell.getYawDifference();
        state.strength = 1 + Mth.sin(caster.asEntity().tickCount + tickDelta) * 0.1F;

        if (client.getCameraEntity() instanceof CastSpellEntity) {
            double distance = caster.asEntity().distanceTo(client.getCameraEntity());
            state.shouldRenderContents =
                    distance <= 50 // don't bother rendering if too far away
                    && distance >= 2; // don't render ourselves
        }
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector vertices, State spell, CasterState caster, int light) {
        super.render(matrices, vertices, spell, caster, light);

        matrices.pushPose();
        matrices.translate(0, 0.02, 0);
        SpellGeometry.render(matrices, vertices, RenderTypes.endGateway(), (stack, buffer) ->
                SphereModel.DISK.render(stack, buffer, light, 0, 2F * spell.strength, CommonColors.WHITE));
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0, -0.02, 0);
        matrices.rotate(Axis.XP.rotationDegrees(180));
        if (Unicopia.getConfig().simplifiedPortals.get() || !spell.destination.isSet() || !spell.shouldRenderContents) {
            drawFallback(matrices, vertices, light, spell.strength);
        } else {
            float grown = Math.min(caster.entityState.ageInTicks, 20) / 20F;
            matrices.scale(grown, 1, grown);
            spell.destination.getTarget().ifPresentOrElse(target -> {
                PortalFrameBuffer buffer = PortalFrameBuffer.get(target.uuid());
                buffer.request(spell);
                if (buffer.hasTexture()) {
                    SpellGeometry.render(matrices, vertices, com.minelittlepony.unicopia.client.render.RenderLayers.getPortal(buffer.textureId()), (stack, vertex) ->
                            SphereModel.DISK.render(stack, vertex, light, 0, 2F * spell.strength, CommonColors.WHITE));
                } else {
                    drawFallback(matrices, vertices, light, spell.strength);
                }
            }, () -> drawFallback(matrices, vertices, light, spell.strength));
        }
        matrices.popPose();
    }

    private static void drawFallback(PoseStack matrices, SubmitNodeCollector vertices, int light, float strength) {
        SpellGeometry.render(matrices, vertices, RenderTypes.endGateway(), (stack, buffer) ->
                SphereModel.DISK.render(stack, buffer, light, 0, 2F * strength, CommonColors.WHITE));
    }

    static class State extends SpellRenderState {
        public boolean shouldRenderContents;
        public final EntityReference<Entity> destination = new EntityReference<>();

        public float yaw;
        public UUID portalUuid;
        public EntityValues<Entity> portalState;

        public Quaternionf orientationChange;
        public Matrix4f positionMatrix;

        public float pitchChange;
        public float yawChange;
        public float strength;
    }
}
