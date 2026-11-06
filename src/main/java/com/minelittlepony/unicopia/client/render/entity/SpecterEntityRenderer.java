package com.minelittlepony.unicopia.client.render.entity;

import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.joml.Quaternionf;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.SpecterEntity;
import com.mojang.blaze3d.vertex.PoseStack;

public class SpecterEntityRenderer extends HumanoidMobRenderer<SpecterEntity, SpecterEntityRenderer.State, SpecterEntityRenderer.SpecterEntityModel> implements HitboxController<SpecterEntity> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/specter.png");

    public SpecterEntityRenderer(Context context) {
        super(context, new SpecterEntityModel(context.bakeLayer(ModelLayers.PLAYER)), 0);
        addLayer(new HeadFeature(this));
        addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
                context.getEquipmentRenderer()));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SpecterEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        state.inverseCameraAngle = camera.rotation();
        state.cameraDistance = entity.distanceToSqr(camera.position());
        state.alpha = state.cameraDistance <= 400 ? 0 : ARGB.as8BitChannel((float)Math.clamp(((state.cameraDistance - 400D) / 600D), 0, 1));

        long now = System.currentTimeMillis();
        if (entity.lastHitboxRenderTime > now - 3000) {
            state.alpha = 0;
        }
        if (entity.lastInViewportTime > now - 100) {
            if (entity.hideInViewportTime < now) {
                state.alpha = 0;
            }
        } else {
            entity.hideInViewportTime = now + 300 + entity.getId();
        }
        entity.lastInViewportTime = now;
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRenderHitbox(SpecterEntity entity) {
        entity.lastHitboxRenderTime = System.currentTimeMillis();
        return false;
    }

    @Override
    protected void setupRotations(State state, PoseStack matrices, float animationProgress, float bodyYaw) {
        super.setupRotations(state, matrices, animationProgress, 0);
        matrices.rotate(state.inverseCameraAngle);
    }

    static class State extends HumanoidRenderState {
        public Quaternionf inverseCameraAngle = new Quaternionf();
        public double cameraDistance;
        public int alpha;
    }

    static class HeadFeature extends RenderLayer<State, SpecterEntityModel> {
        private final SpecterEyesModel model = new SpecterEyesModel(SpecterEyesModel.createModelData().bakeRoot());

        public HeadFeature(RenderLayerParent<State, SpecterEntityModel> context) {
            super(context);
        }

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector vertices, int light, State state, float limbAngle, float limbDistance) {
            if (!state.isInvisible && state.alpha > 0) {
                model.setupAnim(state);
                renderColoredCutoutModel(model, TEXTURE, matrices, vertices, light, state, ARGB.color(state.alpha, CommonColors.WHITE), 0);
            }
        }

    }

    static class SpecterEntityModel extends HumanoidModel<State> {
        public SpecterEntityModel(ModelPart root) {
            super(root, RenderTypes::eyes);
            root.skipDraw = true;
        }
    }

    static class SpecterEyesModel extends EntityModel<State> {
        public SpecterEyesModel(ModelPart root) {
            super(root, RenderTypes::eyes);
        }

        static LayerDefinition createModelData() {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();
            root.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -6, -4, 8, 8, 8, Set.of(Direction.NORTH)), PartPose.ZERO);
            return LayerDefinition.create(data, 64, 32);
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            float scale = 1 + state.alpha / 80F;
            root().visible = state.alpha > 0;
            root().xScale = scale;
            root().yScale = scale;
            root().zScale = scale;
        }
    }
}
