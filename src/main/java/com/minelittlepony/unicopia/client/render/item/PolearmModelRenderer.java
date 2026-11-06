package com.minelittlepony.unicopia.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.joml.Vector3fc;
import org.joml.Vector3f;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemDisplayContext;

public class PolearmModelRenderer implements NoDataSpecialModelRenderer {
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();

        int y = -9;

        PartDefinition pole = root.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(0, 6).addBox(-0.5f, y, -0.5f, 1, 25, 1), PartPose.ZERO);
        pole.addOrReplaceChild("base", CubeListBuilder.create().texOffs(4, 0).addBox(-1.5f, y - 2, -0.5f, 3, 2, 1), PartPose.ZERO);
        pole.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5f, y - 6, -0.5f, 1, 4, 1), PartPose.ZERO);
        return LayerDefinition.create(data, 32, 32);
    }

    private final ModelPart model = getTexturedModelData().bakeRoot();
    private final Identifier texture;

    public PolearmModelRenderer(Identifier texture) {
        this.texture = texture;
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector vertices, int light, int overlay, boolean glint, int color) {
        vertices.submitCustomGeometry(matrices, RenderTypes.entitySolid(texture), (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            model.render(stack, buffer, light, overlay, color);
        });
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-1, -1, -1));
        extents.accept(new Vector3f(1, 1, 1));
    }

    protected void applyTransforms(ItemDisplayContext displayContext, PoseStack matrices) {
        if (displayContext.firstPerson()) {
            matrices.scale(1, -1, -1);
        } else {
            int swap = displayContext.leftHand() ? -1 : 1;
            matrices.scale(1.5F, -1.5F, -1.5F);
            float offsetX = swap * 0.05F;
            matrices.translate(offsetX, 0, 0.05F);
            matrices.rotateAround(Axis.YP.rotationDegrees(-30 * swap), offsetX, 0.5F, offsetX);
            matrices.rotateAround(Axis.ZP.rotationDegrees(-30 * swap), offsetX, 0.5F, offsetX);
        }
    }

    public record Unbaked(Identifier texture) implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<com.minelittlepony.unicopia.client.render.item.PolearmModelRenderer.Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(com.minelittlepony.unicopia.client.render.item.PolearmModelRenderer.Unbaked::texture)
        ).apply(i, com.minelittlepony.unicopia.client.render.item.PolearmModelRenderer.Unbaked::new));

        @Override
        public MapCodec<com.minelittlepony.unicopia.client.render.item.PolearmModelRenderer.Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            return new PolearmModelRenderer(texture);
        }
    }
}
