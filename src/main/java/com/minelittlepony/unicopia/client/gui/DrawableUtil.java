package com.minelittlepony.unicopia.client.gui;

import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

import com.minelittlepony.unicopia.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public interface DrawableUtil {
    double PI = Math.PI;
    double TAU = Math.PI * 2;
    double NUM_RINGS = 300;
    double INCREMENT = TAU / NUM_RINGS;

    static void drawScaledText(GuiGraphicsExtractor context, Component text, int x, int y, float size, int color) {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(size, size);
        context.text(Minecraft.getInstance().font, text, 0, 0, color, false);
        matrices.popMatrix();
    }

    static void renderItemIcon(GuiGraphicsExtractor context ,ItemStack stack, double x, double y, float scale) {
        Matrix3x2fStack modelStack = context.pose();
        modelStack.pushMatrix();
        modelStack.translate((float)x, (float)y);
        if (scale != 1) {
            modelStack.scale(scale, scale);
        }
        context.item(stack, 0, 0);

        modelStack.popMatrix();
    }

    static void renderRaceIcon(GuiGraphicsExtractor context, Race race, int x, int y, int size) {
        context.blit(RenderPipelines.GUI_TEXTURED, race.getIcon(), x - size / 2, y - size / 2, 0, 0, 0, size, size, size, size);
    }

    static void drawLine(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        for (int i = 0; i <= steps; i++) {
            int x = steps == 0 ? x1 : x1 + (x2 - x1) * i / steps;
            int y = steps == 0 ? y1 : y1 + (y2 - y1) * i / steps;
            context.fill(x, y, x + 1, y + 1, color);
        }
    }

    static void drawNotchedArc(GuiGraphicsExtractor context, double innerRadius, double outerRadius, double startAngle, double arcAngle, double notchAngle, double notchSpacing, int color) {
        double endAngle = startAngle + arcAngle;
        for (double angle = startAngle; angle < endAngle; angle += notchAngle + notchSpacing) {
            drawArc(context, innerRadius, outerRadius, angle, Math.min(notchAngle, endAngle - angle), color);
        }
    }

    static void drawArc(GuiGraphicsExtractor context, double innerRadius, double outerRadius, double startAngle, double arcAngle, int color) {
        if (outerRadius <= innerRadius || arcAngle <= 0 || color >>> 24 == 0
                || !Double.isFinite(outerRadius) || !Double.isFinite(startAngle) || !Double.isFinite(arcAngle)) return;
        Matrix3x2f pose = new Matrix3x2f(context.pose());
        int radius = (int)Math.ceil(outerRadius);
        ScreenRectangle scissor = context.scissorStack.peek();
        ScreenRectangle bounds = new ScreenRectangle(-radius, -radius, radius * 2, radius * 2).transformMaxBounds(pose);
        if (scissor != null) bounds = bounds.intersection(scissor);
        if (bounds != null) {
            context.guiRenderState.addGuiElement(new ArcRenderState(pose, Math.max(0, innerRadius), outerRadius,
                    startAngle, Math.min(TAU, arcAngle), color, scissor, bounds));
        }
    }

    record ArcRenderState(Matrix3x2fc pose, double innerRadius, double outerRadius, double startAngle,
            double arcAngle, int color, ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState {
        @Override
        public RenderPipeline pipeline() { return RenderPipelines.GUI; }

        @Override
        public TextureSetup textureSetup() { return TextureSetup.noTexture(); }

        @Override
        public void buildVertices(VertexConsumer vertices) {
            int segments = Math.max(1, (int)Math.ceil(arcAngle / INCREMENT));
            for (int i = 0; i < segments; i++) {
                double a = startAngle + arcAngle * i / segments;
                double b = startAngle + arcAngle * (i + 1) / segments;
                vertex(vertices, innerRadius, a);
                vertex(vertices, outerRadius, a);
                vertex(vertices, outerRadius, b);
                vertex(vertices, innerRadius, b);
            }
        }

        private void vertex(VertexConsumer vertices, double radius, double angle) {
            vertices.addVertexWith2DPose(pose, (float)(-Math.sin(angle) * radius), (float)(Math.cos(angle) * radius)).setColor(color);
        }
    }
}
