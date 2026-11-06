package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.SombraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class SombraEntityRenderer extends LivingEntityRenderer<SombraEntity, SombraEntityRenderer.State, SombraEntityModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/sombra/head.png");

    public SombraEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SombraEntityModel(SombraEntityModel.getTexturedModelData().bakeRoot()), 0);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SombraEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.nameTagAttachment = null;
        state.jawsOpenAmount = entity.getBiteAmount(tickDelta);
        state.scale = entity.getScaleFactor(tickDelta) * 1.7F;
    }

    @Override
    public Identifier getTextureLocation(State entity) {
        return TEXTURE;
    }

    public static class State extends LivingEntityRenderState {
        public float jawsOpenAmount;
        public float scale;
    }
}
