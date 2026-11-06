package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.minelittlepony.unicopia.client.render.RenderGeometry;
import com.minelittlepony.client.render.entity.state.PonyRenderState;

class SpellEffectGear implements Gear<com.minelittlepony.client.render.entity.state.PonyRenderState> {
    private CasterState caster;

    @Override
    public boolean canRender(PonyModel<?> model, PonyRenderState entity) {
        return CasterState.of(entity).present;
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.LEGS;
    }

    @Override
    public Identifier getTexture(PonyRenderState entity, Context<PonyRenderState, ?> context) {
        return BraceletFeatureRenderer.TEXTURE;
    }

    @Override
    public void transform(PonyRenderState state, PonyModel<PonyRenderState> model, PoseStack matrices) {
    }

    @Override
    public void render(PoseStack stack, GearRenderState<PonyRenderState> state, SubmitNodeCollector frame,
            RenderType renderType, int overlay, int light, int color) {
        SpellEffectsRenderDispatcher.INSTANCE.render(stack, frame, light, CasterState.of(state.entityState));
    }
}
