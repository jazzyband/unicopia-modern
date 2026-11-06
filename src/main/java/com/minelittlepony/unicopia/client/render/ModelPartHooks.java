package com.minelittlepony.unicopia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class ModelPartHooks {
    private static final float PIXEL_SCALE = 0.0625F; // 1/16;
    private static Stack<Set<EnqueudHeadRender>> renderCalls = new Stack<>();

    public static void startCollecting() {
        renderCalls.push(new LinkedHashSet<>());
    }

    public static Set<EnqueudHeadRender> stopCollecting() {
        return renderCalls.isEmpty() ? Set.of() : renderCalls.pop();
    }

    public static void onHeadRendered(ModelPart part, PoseStack matrices) {
        if (part.skipDraw || !part.visible || renderCalls.isEmpty()) {
            return;
        }

        var head = renderCalls.peek();
        if (head.size() > 5) {
            renderCalls.pop();
            return;
        }

        final var bestCandidate = new EnqueudHeadRender();

        part.visit(matrices, (entry, name, index, cube) -> {
            float x = cube.maxX - cube.minX;
            float y = cube.maxY - cube.minY;
            float z = cube.maxZ - cube.minZ;

            float volume = div(Math.abs(x * y * z), Math.abs(x + y + z)) * 3F;

            if (volume > 0 && volume > bestCandidate.volume) {
                bestCandidate.cube = cube;
                bestCandidate.transformation = entry;
                bestCandidate.volume = volume;
                bestCandidate.maxSideLength = Math.max(Math.max(x, z), y);
            }
        });

        if (bestCandidate.transformation != null) {
            head.add(bestCandidate);
        }
    }

    static float div(float a, float b) {
        return b == 0 ? 0 : a / b;
    }

    public static final class EnqueudHeadRender {
        private ModelPart.Cube cube;
        private PoseStack.Pose transformation;
        private float volume;
        private float maxSideLength;

        public void transform(PoseStack matrices, float cubeSize) {
            matrices.last().normal().set(transformation.normal());
            matrices.last().pose().set(transformation.pose());

            float x = Mth.lerp(0.5F, cube.minX, cube.maxX);
            float y = Mth.lerp(0.5F, cube.minY, cube.maxY);
            float z = Mth.lerp(0.5F, cube.minZ, cube.maxZ);
            float scale = (maxSideLength / cubeSize) * PIXEL_SCALE;

            matrices.translate(x * PIXEL_SCALE, y * PIXEL_SCALE, z * PIXEL_SCALE);
            matrices.scale(scale, scale, scale);
        }
    }
}
