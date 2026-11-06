package com.minelittlepony.unicopia.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public interface GradientUtil {
    static void fillVerticalGradient(GuiGraphicsExtractor context, int startX, int startY, int stopY, int endX, int endY, int colorStart, int colorStop, int colorEnd, int z) {
        context.fillGradient(startX, startY, endX, stopY, colorStart, colorStop);
        context.fillGradient(startX, stopY, endX, endY, colorStop, colorEnd);
    }

    static void fillRadialGradient(GuiGraphicsExtractor context, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z, float radius) {
        int width = endX - startX;
        int height = endY - startY;
        context.fill(startX, startY, endX, endY, colorStart);
        float inner = Mth.clamp(1 - radius, 0, 1);
        for (int i = 1; i <= 24; i++) {
            float fraction = i / 24F;
            float inset = fraction * 0.5F;
            int x1 = startX + (int)(width * inset);
            int y1 = startY + (int)(height * inset);
            int x2 = endX - (int)(width * inset);
            int y2 = endY - (int)(height * inset);
            if (x1 >= x2 || y1 >= y2) break;
            float colorFraction = Mth.clamp((fraction - inner) / Math.max(0.001F, 1 - inner), 0, 1);
            context.fill(x1, y1, x2, y2, ARGB.srgbLerp(colorFraction, colorStart, colorEnd));
        }
    }
}
