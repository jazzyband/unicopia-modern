package com.minelittlepony.unicopia.client.render;

import java.util.function.BiConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class RenderGeometry {
    private RenderGeometry() {}

    public static void submitModel(PoseStack matrices, SubmitNodeCollector collector, Model<?> model,
            RenderType layer, int light, int overlay, int color) {
        submit(matrices, collector, layer, (stack, buffer) -> model.renderToBuffer(stack, buffer, light, overlay, color));
    }

    public static void submit(PoseStack matrices, SubmitNodeCollector collector, RenderType layer,
            BiConsumer<PoseStack, VertexConsumer> geometry) {
        collector.submitCustomGeometry(matrices, layer, (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            geometry.accept(stack, buffer);
        });
    }
}
