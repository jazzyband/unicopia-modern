package com.minelittlepony.unicopia.client.render.entity;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.MimicEntity;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.entity.Pose;

public class MimicEntityRenderer extends MobRenderer<MimicEntity, MimicEntityRenderer.State, MimicEntityRenderer.MimicModel> implements HitboxController<MimicEntity> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/chest/normal.png");

    public MimicEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new MimicModel(MimicModel.getTexturedModelData().bakeRoot()), 0);

    }

    @Override

    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MimicEntity entity, State state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.peekAmount = entity.getPeekAmount();
        state.legAnimationProgress = entity.walkAnimation.position(tickDelta);
        state.legAmplitude = entity.walkAnimation.speed(tickDelta);
        state.bodyTilt = Mth.cos(state.legAnimationProgress * 0.6662F) * 1.4F * state.legAmplitude * 10 * state.peekAmount;

        state.tickDelta = tickDelta;

        state.tileData = entity.getChestData();
        if (state.tileData != null) {
            state.tileData.setLevel(entity.level());
            ((MixinBlockEntity)state.tileData).setPos(entity.blockPosition());

            var properties = CloudChestBlockEntityRenderer.getProperties(state.tileData.getBlockState(), state.tileData);
            float progress = 1 - (float)Math.pow(1 - properties.apply(ChestBlock.opennessCombiner(state.tileData)).get(tickDelta), 3);
            state.mouthOpenAmount = -(progress * 1.5707964f);
        } else {
            state.mouthOpenAmount = 0;
        }

        if (state.peekAmount < 0.3F) {
            state.peekAmount = 0;
            state.bodyRot = entity.getDirection().toYRot();
        }
        state.targeted = Minecraft.getInstance().crosshairPickEntity == entity;
        state.chestState = state.tileData == null ? null : Minecraft.getInstance().getBlockEntityRenderDispatcher().tryExtractRenderState(state.tileData, tickDelta, null, false);
    }

    @Override
    public boolean shouldRenderHitbox(MimicEntity entity) {
        return entity.getPeekAmount() > 0.3F;
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        matrices.pushPose();
        matrices.translate(0, 0.3F * state.peekAmount, 0);
        matrices.rotate(Axis.XP.rotationDegrees(state.bodyTilt));
        super.submit(state, matrices, vertices, camera);
        matrices.popPose();

        if (state.chestState != null) {
            matrices.pushPose();
            matrices.rotate(Axis.ZP.rotationDegrees(180));
            matrices.rotate(Axis.XP.rotationDegrees(-state.xRot));
            matrices.translate(-0.5, -1.5, -0.5);
            Minecraft.getInstance().getBlockEntityRenderDispatcher().submit(state.chestState, matrices, vertices, camera);
            matrices.popPose();
        }
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees() {
        return 0;
    }

    public static class State extends LivingEntityRenderState {
        public float peekAmount;
        public float legAnimationProgress;
        public float legAmplitude;
        public float bodyTilt;
        public float mouthOpenAmount;
        @Nullable
        public ChestBlockEntity tileData;
        @Nullable
        public BlockEntityRenderState chestState;
        public float tickDelta;
        public boolean targeted;
    }

    static class MimicModel extends EntityModel<MimicEntityRenderer.State> {
        private ModelPart lid;
        private ModelPart leftLeg;
        private ModelPart rightLeg;

        public MimicModel(ModelPart part) {
            super(part);
            this.lid = part.getChild("lid");
            this.leftLeg = part.getChild("left_leg");
            this.rightLeg = part.getChild("right_leg");
        }

        public static LayerDefinition getTexturedModelData() {
            MeshDefinition data = new MeshDefinition();
            PartDefinition root = data.getRoot();
            PartDefinition lid = root.addOrReplaceChild("lid", CubeListBuilder.create(), PartPose.offsetAndRotation(0, 15, -7, 0, 0, -3.1416F));
            lid.addOrReplaceChild("tongue", CubeListBuilder.create()
                    .texOffs(11, 34).addBox(-3, -11, 1, 6, 1, 8, CubeDeformation.NONE), PartPose.offsetAndRotation(0, 6, 9, 0.8F, 0, 0));
            lid.addOrReplaceChild("upper_teeth", CubeListBuilder.create()
                    //.uv(0, 0).cuboid(-7, 0, 0, 14, 5, 14, Dilation.NONE) //lid
                    //.uv(0, 0).cuboid(-1, -2.0F, 14.0F, 2.0F, 4.0F, 1.0F) //lock
                    .texOffs(0, 0).addBox(-1, -2, 12, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(-4, -2, 12, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(2, -2, 12, 2, 4, 1, CubeDeformation.NONE), PartPose.ZERO)
                    .addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5, -2, -6, 2, 4, 1, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox(-8, -2, -6, 2, 4, 1, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox(-11, -2, -6, 2, 4, 1, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox(-5, -2, 5, 2, 4, 1, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox(-8, -2, 5, 2, 4, 1, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox(-11, -2, 5, 2, 4, 1, CubeDeformation.NONE), PartPose.offsetAndRotation(0, 0, 0, 0, 1.5708F, 0));
            //root.addChild("base", ModelPartBuilder.create().uv(0, 19).cuboid(1, 0, 1, 14, 10, 14), ModelTransform.of(8, 24, -8, 0, 0, -3.1416F));
            root.addOrReplaceChild("lower_teeth", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1, -1, 12, 2, 4, 1, CubeDeformation.NONE)
                .texOffs(0, 0).addBox(-4, -1, 12, 2, 4, 1, CubeDeformation.NONE)
                .texOffs(0, 0).addBox(2, -1, 12, 2, 4, 1, CubeDeformation.NONE), PartPose.offset(0, 13, -7))
                .addOrReplaceChild("cube_r2", CubeListBuilder.create()
                    .texOffs(0, 0).addBox(-6, -1, -6, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(-9, -1, -6, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(-12, -1, -6, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(-6, -1, 5, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(-9, -1, 5, 2, 4, 1, CubeDeformation.NONE)
                    .texOffs(0, 0).addBox(-12, -1, 5, 2, 4, 1, CubeDeformation.NONE), PartPose.offsetAndRotation(0, 0, 0, 0, 1.5708F, 0));
            root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                    .texOffs(7, 30).addBox(-2.5F, -1.5F, -3.5F, 5, 7, 6, CubeDeformation.NONE), PartPose.offset(3.5F, 23.5F, 0));
            root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                    .texOffs(7, 30).mirror().addBox(-9.5F, -1.5F, -3.5F, 5, 7, 6, CubeDeformation.NONE), PartPose.offset(3.5F, 23.5F, 0));
            return LayerDefinition.create(data, 64, 64);
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            root().yRot = Mth.DEG_TO_RAD * 180;
            root().xRot = -state.xRot * Mth.DEG_TO_RAD;
            lid.xRot = state.mouthOpenAmount;
            rightLeg.resetPose();
            leftLeg.resetPose();
            rightLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
            leftLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * state.walkAnimationSpeed;
            float revealPercentage = state.peekAmount;
            float velocy = (1 - revealPercentage) * -10F;
            rightLeg.y += velocy;
            leftLeg.y += velocy;
            rightLeg.visible = revealPercentage > 0.2F;
            leftLeg.visible = revealPercentage > 0.2F;
        }
    }
}