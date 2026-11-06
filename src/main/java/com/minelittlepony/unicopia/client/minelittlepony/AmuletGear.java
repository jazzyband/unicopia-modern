package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.AmuletFeatureRenderer.AmuletModel;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.minelittlepony.unicopia.client.render.RenderGeometry;
import com.minelittlepony.client.render.entity.state.PonyRenderState;

class AmuletGear extends AmuletModel implements Gear<com.minelittlepony.client.render.entity.state.PonyRenderState> {

    private final Map<ResourceKey<Item>, Identifier> textures = new HashMap<>();

    public AmuletGear() {
        super(AmuletModel.getData(new CubeDeformation(0.3F)).bakeRoot());
    }

    @Override
    public boolean canRender(PonyModel<?> model, PonyRenderState entity) {
        return !CasterState.of(entity).amulet.empty;
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.BODY;
    }

    @Override
    public Identifier getTexture(PonyRenderState entity, Context<PonyRenderState, ?> context) {
        return textures.computeIfAbsent(CasterState.of(entity).amulet.key, key -> key.identifier().withPath(p  -> "textures/models/armor/" + p + ".png"));
    }

    @Override
    public void transform(PonyRenderState state, PonyModel<PonyRenderState> model, PoseStack matrices) {
        BodyPart part = getGearLocation();
        model.transform(state, part, matrices);
        matrices.translate(0, 0.25, 0);
    }

    @Override
    public void render(PoseStack stack, GearRenderState<PonyRenderState> state, SubmitNodeCollector frame,
            RenderType renderType, int overlay, int light, int color) {
        if (state.model instanceof HumanoidModel<?> biped) {
            setAngles(biped);
        }
        RenderGeometry.submitModel(stack, frame, this, renderType, light, overlay, color);
    }
}
