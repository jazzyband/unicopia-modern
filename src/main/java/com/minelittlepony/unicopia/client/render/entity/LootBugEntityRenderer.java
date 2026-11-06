package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Silverfish;

public class LootBugEntityRenderer extends SilverfishRenderer {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/loot_bug.png");

    public LootBugEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState entity) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(Silverfish entity, LivingEntityRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.scale *= 2;
    }
}
