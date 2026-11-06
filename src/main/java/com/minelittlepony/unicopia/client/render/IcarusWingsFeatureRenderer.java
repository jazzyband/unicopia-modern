package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public class IcarusWingsFeatureRenderer<S extends HumanoidRenderState> extends WingsFeatureRenderer<S> {
    private static final Identifier ICARUS_WINGS = Unicopia.id("textures/models/wings/icarus.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = Unicopia.id("textures/models/wings/icarus_corrupted.png");

    public IcarusWingsFeatureRenderer(RenderLayerParent<S, ? extends HumanoidModel<S>> context) {
        super(context);
    }

    @Override
    protected boolean canRender(S state) {
        return !super.canRender(state) && CasterState.of(state).pegasusAmulet;
    }

    @Override
    protected RenderType getLayer(Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    protected Identifier getTexture(S state) {
        return CasterState.of(state).inHell ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
    }

    @Override
    @Nullable
    protected Identifier getOverlayTexture(S state) {
        return null;
    }
}
