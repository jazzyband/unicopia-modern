package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.compat.pehkui.PehkUtil;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

class EntityDisguiseRenderer {
    private final WorldRenderDelegate delegate;

    EntityDisguiseRenderer(WorldRenderDelegate delegate) {
        this.delegate = delegate;
    }

    void prepare(Living<?> pony, Disguise disguise) {
        if (!delegate.client.isPaused()) {
            disguise.update(pony, false);
        }
        EntityAppearance appearance = disguise.getAppearance();
        Entity entity = appearance.getEntity();
        if (entity == null) {
            return;
        }
        BlockEntity blockEntity = appearance.getBlockEntity();
        if (blockEntity != null) {
            blockEntity.setLevel(entity.level());
        }
        int fireTicks = pony.asEntity().displayFireAnimation() ? 1 : 0;
        if (delegate.client.getDebugOverlay().showDebugScreen()) {
            entity.setBoundingBox(pony.asEntity().getBoundingBox());
        }
        entity.setRemainingFireTicks(fireTicks);
        PehkUtil.copyScale(pony.asEntity(), entity);
        appearance.getAttachments().forEach(attachment -> {
            PehkUtil.copyScale(pony.asEntity(), attachment.entity());
            attachment.entity().setRemainingFireTicks(fireTicks);
        });
    }

    void render(WorldRenderDelegate.EntitySubmitter submitter, EntityAppearance appearance,
            double x, double y, double z, float tickDelta, PoseStack matrices,
            SubmitNodeCollector vertices, CameraRenderState camera) {
        Entity entity = appearance.getEntity();
        if (entity == null) {
            return;
        }
        Vec3 cameraPos = camera.pos;
        if (appearance.isAxisAligned() && (x != 0 || y != 0 || z != 0)) {
            x = Mth.lerp(tickDelta, entity.xOld, entity.getX()) - cameraPos.x;
            y = Mth.lerp(tickDelta, entity.yOld, entity.getY()) - cameraPos.y;
            z = Mth.lerp(tickDelta, entity.zOld, entity.getZ()) - cameraPos.z;
        }
        BlockEntity blockEntity = appearance.getBlockEntity();
        if (blockEntity == null || !renderBlockEntity(updateBlockEntity(blockEntity, entity),
                matrices, vertices, camera, x, y, z, tickDelta)) {
            submitter.submit(entity, x, y, z);
        }
        Vec3 origin = entity.position().subtract(x, y, z);
        appearance.getAttachments().forEach(attachment -> {
            Entity attached = attachment.entity();
            Vec3 pos = attached.position().subtract(origin);
            if (blockEntity == null || !renderBlockEntity(updateBlockEntity(blockEntity, attached),
                    matrices, vertices, camera, pos.x, pos.y, pos.z, tickDelta)) {
                submitter.submit(attached, pos.x, pos.y, pos.z);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private BlockEntity updateBlockEntity(BlockEntity blockEntity, Entity reference) {
        ((MixinBlockEntity)blockEntity).setPos(reference.blockPosition());
        if (reference instanceof FallingBlockEntity falling) {
            blockEntity.setBlockState(falling.getBlockState());
        }
        return blockEntity;
    }

    private boolean renderBlockEntity(BlockEntity blockEntity, PoseStack matrices,
            SubmitNodeCollector vertices, CameraRenderState camera,
            double x, double y, double z, float tickDelta) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        var state = dispatcher.tryExtractRenderState(blockEntity, tickDelta, null, false);
        if (state != null) {
            matrices.pushPose();
            matrices.translate(x - 1, y, z - 1);
            dispatcher.submit(state, matrices, vertices, camera);
            matrices.popPose();
        }
        return blockEntity.getBlockState().getRenderShape() != RenderShape.INVISIBLE;
    }

    @Nullable
    static HumanoidModel<?> getBipedModel(Entity entity) {
        if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer<?, ?, ?> renderer
                && renderer.getModel() instanceof HumanoidModel<?> biped) {
            return biped;
        }
        return null;
    }
}
