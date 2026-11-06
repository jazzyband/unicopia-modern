package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.DarkVortexSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.PlaneModel;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class DarkVortexSpellRenderer extends SpellRenderer<DarkVortexSpell, DarkVortexSpellRenderer.State> {
    private static final Identifier ACCRETION_DISK_TEXTURE = Unicopia.id("textures/spells/dark_vortex/accretion_disk.png");
    private static final int DISTORTION_ZONE_COLOR = ARGB.color(CommonColors.BLACK, (int)(255 * 0.9F));

    private static float cameraDistortion;

    public static float getCameraDistortion() {
        cameraDistortion *= 0.9F;
        cameraDistortion = Mth.clamp(cameraDistortion, 0, 80);
        return cameraDistortion;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(DarkVortexSpell spell, State state, Caster<?> caster, float tickDelta) {
        super.updateRenderState(spell, state, caster, tickDelta);
        state.radius = (float)spell.getEventHorizonRadius();
        state.range = (float)spell.getDrawDropOffRange() / 8F;
        state.yOffset = spell.getYOffset();
        state.origin = spell.getOrigin(caster);
    }

    public static class State extends SpellRenderState {
        public float radius;
        public float range;
        public double yOffset;

        public Vec3 origin = Vec3.ZERO;
    }

    @Override
    public boolean shouldRenderEffectPass(int pass) {
        return pass < 2;
    }

    @Override
    public void render(PoseStack matrices, SubmitNodeCollector vertices, State state, CasterState caster, int light) {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();

        Vec3 ray = camera.position().subtract(state.origin);

        float absDistance = (float)ray.length();

        matrices.pushPose();
        matrices.translate(0, state.yOffset, 0);
        matrices.rotate(Axis.YN.rotationDegrees(caster.yawOffset));

        float visualRadius = Math.min(state.radius * 0.8F, absDistance - 1F);

        SpellGeometry.render(matrices, vertices, RenderLayers.getMagicNoColor(), (stack, buffer) -> SphereModel.SPHERE.render(stack, buffer, light, 1, visualRadius, CommonColors.BLACK));
        SpellGeometry.render(matrices, vertices, RenderLayers.getMagicColored(), (stack, buffer) -> SphereModel.SPHERE.render(stack, buffer, light, 1, visualRadius + 0.05F, DISTORTION_ZONE_COLOR));
        SpellGeometry.render(matrices, vertices, RenderLayers.getMagicColored(), (stack, buffer) -> SphereModel.SPHERE.render(stack, buffer, light, 1, visualRadius + 0.1F, DISTORTION_ZONE_COLOR));
        SpellGeometry.render(matrices, vertices, RenderLayers.getMagicColored(), (stack, buffer) -> SphereModel.SPHERE.render(stack, buffer, light, 1, visualRadius + 0.15F, DISTORTION_ZONE_COLOR));

        matrices.pushPose();

        float distance = 1F / Mth.clamp(absDistance / (state.radius + 7), 0.0000001F, 1);
        distance *= distance;
        if (absDistance < state.radius * 4) {
            cameraDistortion += distance;
        }

        SpellGeometry.render(matrices, vertices, RenderTypes.endPortal(), (stack, buffer) -> SphereModel.DISK.render(stack, buffer, light, 1, state.radius * 0.5F, 0));

        if (state.radius > 0.3F && absDistance > state.radius) {
            double g = Math.sqrt(ray.x * ray.x + ray.z * ray.z);
            float age = caster.entityState.ageInTicks;
            float pitch = Mth.wrapDegrees((float)(-(Mth.atan2(ray.y, g) * 180.0F / (float)Math.PI)));
            float yaw = Mth.wrapDegrees((float)(Mth.atan2(ray.z, ray.x) * 180.0F / (float)Math.PI) - 90.0F);

            matrices.rotate(Axis.YP.rotationDegrees(-caster.yawOffset - yaw));
            matrices.rotate(Axis.XP.rotationDegrees(-pitch));

            float processionSpeed = age * 0.02F;
            float maxProcessionAngle = 15;

            float cosProcession = Mth.cos(processionSpeed);
            float sinProcession = Mth.sin(processionSpeed);

            matrices.scale(state.range, state.range, state.range);

            matrices.rotate(Axis.XP.rotationDegrees(cosProcession * maxProcessionAngle));
            matrices.rotate(Axis.YP.rotationDegrees(sinProcession * maxProcessionAngle));
            matrices.rotate(Axis.ZP.rotationDegrees(age * 18));


            SpellGeometry.render(matrices, vertices, RenderTypes.entityTranslucent(ACCRETION_DISK_TEXTURE), (stack, buffer) -> PlaneModel.INSTANCE.render(stack, buffer, light, 0, 1, CommonColors.WHITE));
            float secondaryScale = 0.9F + cosProcession * 0.3F;
            matrices.translate(0, 0, 0.0001F);
            matrices.scale(secondaryScale, secondaryScale, secondaryScale);
            matrices.rotate(Axis.ZP.rotationDegrees(33));
            SpellGeometry.render(matrices, vertices, RenderTypes.entityTranslucent(ACCRETION_DISK_TEXTURE), (stack, buffer) -> PlaneModel.INSTANCE.render(stack, buffer, light, 0, 1, CommonColors.WHITE));
            matrices.translate(0, 0, 0.0001F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            matrices.rotate(Axis.ZP.rotationDegrees(33));
            SpellGeometry.render(matrices, vertices, RenderTypes.entityTranslucent(ACCRETION_DISK_TEXTURE), (stack, buffer) -> PlaneModel.INSTANCE.render(stack, buffer, light, 0, 1, CommonColors.WHITE));
        }
        matrices.popPose();
        matrices.popPose();
    }

    @Override
    protected void renderCountdown(PoseStack matrices, SubmitNodeCollector vertices, TimedSpell spell, float tickDelta) {

    }
}
