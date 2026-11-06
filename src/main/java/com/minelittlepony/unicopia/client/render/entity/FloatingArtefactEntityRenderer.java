package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.entity.mob.FloatingArtefactEntity;
import com.minelittlepony.unicopia.entity.mob.StationaryObjectEntity;
import com.minelittlepony.unicopia.item.UItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Mth;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FloatingArtefactEntityRenderer extends EntityRenderer<FloatingArtefactEntity, FloatingArtefactEntityRenderer.State> {

    private final ItemModelResolver itemModelManager;

    public FloatingArtefactEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        itemModelManager = ctx.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FloatingArtefactEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);

        ItemStack stack = entity.getStack();

        if (stack.isEmpty()) {
            stack = UItems.EMPTY_JAR.getDefaultInstance();
        }

        state.stack = stack;
        state.variance = 0.25F;
        state.scale = 1.6F;
        state.verticalOffset = entity.getVerticalOffset(tickDelta);
        state.yaw = entity.getRotation(tickDelta);
        state.destructionStage = getDestructionStage(entity);
        itemModelManager.updateForTopItem(state.itemState, stack, ItemDisplayContext.GROUND, entity.level(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        matrices.pushPose();
        matrices.scale(state.scale, state.scale, state.scale);
        matrices.translate(0, state.verticalOffset + state.variance * state.modelScaleY, 0);
        matrices.rotate(Axis.YP.rotationDegrees(state.yaw));
        state.itemState.submit(matrices, vertices, state.lightCoords, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);
        matrices.popPose();
        super.submit(state, matrices, vertices, camera);
    }

    public static class State extends EntityRenderState {
        public float variance = 0.25F;
        public float scale = 1.6F;
        public ItemStack stack;
        public final ItemStackRenderState itemState = new ItemStackRenderState();
        public float verticalOffset;
        public float modelScaleY;
        public float yaw;

        public int destructionStage;
    }

    static int getDestructionStage(StationaryObjectEntity entity) {
        return getDestructionStage(entity.getHealth(), entity.getMaxHealth());
    }

    static int getDestructionStage(LivingEntity entity) {
        return getDestructionStage(entity.getHealth(), entity.getMaxHealth());
    }

    static int getDestructionStage(float health, float maxHealth) {
        return (int)(Mth.clamp(1F - (health / maxHealth), 0F, 1F) * 9F);
    }

}
