package com.minelittlepony.unicopia.client.render.spell;

import java.util.function.BiConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

final class SpellGeometry {
    private SpellGeometry() {}

    static void render(PoseStack matrices, SubmitNodeCollector collector, RenderType layer,
            BiConsumer<PoseStack, VertexConsumer> geometry) {
        collector.submitCustomGeometry(matrices, layer, (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            geometry.accept(stack, buffer);
        });
    }
}
