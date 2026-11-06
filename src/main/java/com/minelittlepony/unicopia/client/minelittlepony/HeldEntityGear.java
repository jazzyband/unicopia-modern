package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.HeldEntityFeatureRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.minelittlepony.unicopia.client.render.RenderGeometry;
import com.minelittlepony.client.render.entity.state.PonyRenderState;

class HeldEntityGear extends HeldEntityFeatureRenderer<HumanoidRenderState, LivingEntity> implements Gear<com.minelittlepony.client.render.entity.state.PonyRenderState> {

    private HumanoidRenderState entity;

    public HeldEntityGear() {
        super(null);
    }

    @Override
    public boolean canRender(PonyModel<?> model, PonyRenderState entity) {
        return entity instanceof HumanoidRenderState;
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.BODY;
    }

    @Override
    public Identifier getTexture(PonyRenderState entity, Context<PonyRenderState, ?> context) {
        return net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation();
    }

    @Override
    public void transform(PonyRenderState state, PonyModel<PonyRenderState> model, PoseStack matrices) {
        // noop
    }

    @Override
    public void render(PoseStack matrices, GearRenderState<PonyRenderState> state, SubmitNodeCollector frame,
            RenderType renderType, int overlay, int light, int color) {
        render(matrices, frame, light, state.entityState, state.limbAngle, state.limbDistance);
    }
}

