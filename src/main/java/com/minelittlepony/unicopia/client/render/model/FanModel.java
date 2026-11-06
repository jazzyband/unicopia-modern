package com.minelittlepony.unicopia.client.render.model;

import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class FanModel extends BakedModel {

    public FanModel(TextureAtlasSprite sprite) {
        RenderUtil.Vertex[] dorito = createDorito(sprite);
        PoseStack matrices = new PoseStack();
        for (int d = 0; d < 12; d++) {
            matrices.pushPose();

            matrices.rotate(Axis.ZP.rotationDegrees(30 * d));
            matrices.rotate(Axis.XP.rotationDegrees(15));
            matrices.rotate(Axis.YP.rotationDegrees(15 * d));
            matrices.translate(2.9F, 0, 0);
            for (RenderUtil.Vertex corner : dorito) {
                var position = corner.position(matrices.last().pose());
                addVertex(position.x, position.y(), position.z(), corner.texture().x, corner.texture().y);
            }
            matrices.popPose();
        }
    }

    static RenderUtil.Vertex[] createDorito(TextureAtlasSprite sprite) {
        float chunkSize = 1F;
        float baseLength = 0.8F;
        float uLength = sprite.getU1() - sprite.getU0();
        return new RenderUtil.Vertex[]{
                new RenderUtil.Vertex(-chunkSize, -chunkSize * baseLength, 0, sprite.getU0() + uLength * baseLength, sprite.getV0()),
                new RenderUtil.Vertex( chunkSize,  0, 0, sprite.getU1(), sprite.getV1()),
                new RenderUtil.Vertex(-chunkSize,  chunkSize * baseLength, 0, sprite.getU0(), sprite.getV0()),
                new RenderUtil.Vertex(-chunkSize * 3, 0, 0, sprite.getU0(), sprite.getV1())
        };
    }
}
