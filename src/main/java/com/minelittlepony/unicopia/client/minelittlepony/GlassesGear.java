package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.GlassesFeatureRenderer.GlassesModel;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.minelittlepony.unicopia.client.render.RenderGeometry;
import com.minelittlepony.client.render.entity.state.PonyRenderState;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

class GlassesGear extends GlassesModel implements Gear<com.minelittlepony.client.render.entity.state.PonyRenderState> {

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    public GlassesGear() {
        super(GlassesModel.getData(new CubeDeformation(0.3F), -6, -6).bakeRoot());
    }

    @Override
    public boolean canRender(PonyModel<?> model, PonyRenderState entity) {
        return !CasterState.of(entity).eyewear.stack().isEmpty();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.HEAD;
    }

    @Override
    public Identifier getTexture(PonyRenderState entity, Context<PonyRenderState, ?> context) {
        return textures.computeIfAbsent(CasterState.of(entity).eyewear.stack().getItem().builtInRegistryHolder().unwrapKey().get().identifier(), id -> id.withPath(p -> "textures/models/armor/" + p + ".png"));
    }

    @Override
    public void render(PoseStack stack, GearRenderState<PonyRenderState> state, SubmitNodeCollector frame,
            RenderType renderType, int overlay, int light, int color) {
        if (state.model instanceof net.minecraft.client.model.HumanoidModel<?> biped) {
            setAngles(state.entityState, biped);
        }
        RenderGeometry.submitModel(stack, frame, this, renderType, light, overlay, color);
    }
}
