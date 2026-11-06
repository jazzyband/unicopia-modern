package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer.BraceletModel;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.compat.trinkets.SlotKey;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.minelittlepony.unicopia.client.render.RenderGeometry;
import com.minelittlepony.client.render.entity.state.PonyRenderState;

class BangleGear implements Gear<com.minelittlepony.client.render.entity.state.PonyRenderState> {
    @Nullable
    private CasterState.BangleState state;
    private boolean alex;

    private final BraceletModel steveModel;
    private final BraceletModel alexModel;

    private final SlotKey slot;

    public BangleGear(SlotKey slot) {
        this.slot = slot;
        CubeDeformation dilation = new CubeDeformation(0.3F);
        steveModel = new BraceletModel(BraceletModel.getData(dilation, false, -1, 4, 0).bakeRoot());
        alexModel = new BraceletModel(BraceletModel.getData(dilation, true, -1, 4, 0).bakeRoot());
    }

    @Override
    public boolean canRender(PonyModel<?> model, PonyRenderState state) {
        return !(slot == TrinketsDelegate.MAIN_GLOVE ? CasterState.of(state).mainhandBangle : CasterState.of(state).offhandBangle).empty;
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
        BodyPart part = getGearLocation();
        model.transform(state, part, matrices);
    }

    @Override
    public void render(PoseStack stack, GearRenderState<PonyRenderState> state, SubmitNodeCollector frame,
            RenderType renderType, int overlay, int light, int color) {
        PonyRenderState entity = state.entityState;
        boolean alex = entity.skin.model() == net.minecraft.world.entity.player.PlayerModelType.SLIM;
        CasterState.BangleState bangle = slot == TrinketsDelegate.MAIN_GLOVE
                ? CasterState.of(entity).mainhandBangle : CasterState.of(entity).offhandBangle;
        BraceletModel model = alex ? alexModel : steveModel;
        if (state.model instanceof HumanoidModel<?> biped) {
            model.setAngles(biped);
        }
        HumanoidArm mainArm = entity.mainArm;
        model.setVisible(slot == TrinketsDelegate.MAIN_GLOVE ? mainArm : mainArm.getOpposite());
        RenderGeometry.submitModel(stack, frame, model, renderType,
                bangle.glowing ? 0x0F00F0 : light, overlay, bangle.color);
    }
}
