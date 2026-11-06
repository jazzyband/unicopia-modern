package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.CommonColors;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LevitatingItemEntityRenderer extends EntityRenderer<LevitatingItemEntity, LevitatingItemEntityRenderer.State> {

    private final ItemModelResolver itemModelManager;

    public LevitatingItemEntityRenderer(Context ctx) {
        super(ctx);
        itemModelManager = ctx.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LevitatingItemEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.yaw = entity.getViewYRot(tickDelta);
        state.miningPos = entity.getMiningPos().orElse(null);
        state.miningFace = entity.getMiningFace();
        state.stack = entity.getStack();
        state.yOffset = Mth.sin(state.ageInTicks / 20F) * 0.02F;
        itemModelManager.updateForTopItem(state.stackState, state.stack, ItemDisplayContext.GROUND, entity.level(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        super.submit(state, matrices, vertices, camera);

        if (!state.stack.isEmpty()) {
            float scale = 1.4F;
            matrices.pushPose();
            matrices.translate(0, state.yOffset, 0);
            matrices.scale(scale, scale, scale);
            matrices.rotate(Axis.YP.rotationDegrees(state.yaw));

            if (state.miningPos != null) {
                matrices.rotate(Axis.YP.rotationDegrees(90));
                matrices.rotate(Axis.ZP.rotationDegrees(90 * Mth.sin(state.ageInTicks)));
                matrices.rotate(Axis.XP.rotationDegrees(10 * Mth.sin(state.ageInTicks)));
            }

            state.stackState.submit(matrices, vertices, state.lightCoords, OverlayTexture.NO_OVERLAY, CommonColors.WHITE);

            /*matrices.scale(1.2F, 1.2F, 1.2F);

            itemRenderer.renderItem(state.stack, ModelTransformationMode.GROUND, false, matrices, layer -> {
                return vertices.getBuffer(RenderLayerUtil.getTexture(layer)
                        .map(texture -> RenderLayers.getMagicColored(texture, RenderLayers.DEFAULT_MAGIC_COLOR))
                        .orElse(RenderLayers.getMagicColored()));
            }, light, OverlayTexture.DEFAULT_UV, state.stackModel);*/

            matrices.popPose();


        }
    }

    public static class State extends EntityRenderState {
        @Nullable
        public BlockPos miningPos;
        public Direction miningFace = Direction.UP;
        public ItemStack stack = ItemStack.EMPTY;
        public final ItemStackRenderState stackState = new ItemStackRenderState();
        public float yaw;
        public float yOffset;
    }
}
