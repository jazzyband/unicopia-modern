package com.minelittlepony.unicopia.client.render.model;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BakedModel {
    protected final List<RenderUtil.Vertex> vertices = new ObjectArrayList<>();

    private final Matrix4f textureMatrix = new Matrix4f();

    public Matrix4f getTextureMatrix() {
        return textureMatrix;
    }

    public void scaleUV(float uScale, float vScale) {
        getTextureMatrix().scale(uScale, vScale, 1);
    }

    protected void addVertex(Vector4f vertex) {
        addVertex(vertex.x, vertex.y, vertex.z, (vertex.x + 1) * 0.5F, (vertex.z + 1) * 0.5F);
    }

    protected void addVertex(float x, float y, float z, float u, float v) {
        vertices.add(new RenderUtil.Vertex(x, y, z, u, v));
    }

    public final void render(PoseStack matrices, VertexConsumer buffer, int light, int overlay, float scale, int color) {
        scale = Math.abs(scale);
        if (scale < 0.001F) {
            return;
        }

        matrices.pushPose();
        matrices.scale(scale, scale, scale);
        Matrix4f positionmatrix = matrices.last().pose();
        for (RenderUtil.Vertex vertex : vertices) {
            Vector4f pos = vertex.position(positionmatrix);
            Vector4f tex = vertex.texture(textureMatrix);
            buffer.addVertex(pos.x, pos.y, pos.z, color, tex.x, tex.y, overlay, light, 0, 0, 0);
        }
        matrices.popPose();
        textureMatrix.identity();
    }

    public final void render(PoseStack matrices, VertexConsumer buffer, int light, float scale, int color) {
        scale = Math.abs(scale);
        if (scale < 0.001F) {
            return;
        }

        matrices.pushPose();
        matrices.scale(scale, scale, scale);
        Matrix4f positionmatrix = matrices.last().pose();
        for (RenderUtil.Vertex vertex : vertices) {
            Vector4f pos = vertex.position(positionmatrix);
            Vector4f tex = vertex.texture(textureMatrix);
            buffer.addVertex(pos.x, pos.y, pos.z).setUv(tex.x, tex.y).setColor(color).setLight(getLightAt(pos, light));
        }
        matrices.popPose();
        textureMatrix.identity();
    }

    protected int getLightAt(Vector4f pos, int light) {
        return light;
    }
}
