package com.minelittlepony.unicopia.util;

import static net.minecraft.util.ARGB.*;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;

public interface ColorHelper {
    static int getRainbowColor(Entity entity, int speed, float tickDelta) {
        int n = entity.tickCount / speed + entity.getId();
        int o = DyeColor.values().length;
        int p = n % o;
        int q = (n + 1) % o;
        float r = (entity.tickCount % speed + tickDelta) / 25.0f;
        int fs = DyeColor.byId(p).getTextureDiffuseColor();
        int gs = DyeColor.byId(q).getTextureDiffuseColor();
        return srgbLerp(r, fs, gs);
    }

    static float[] changeSaturation(float red, float green, float blue, float intensity) {
        float avg = (red + green + blue) / 3F;
        float r = avg + (red - avg) * intensity;
        float g = avg + (green - avg) * intensity;
        float b = avg + (blue - avg) * intensity;

        if (r > 1) {
            g -= r - 1;
            b -= r - 1;
            r = 1;
        }
        if (g > 1) {
            r -= g - 1;
            b -= g - 1;
            g = 1;
        }
        if (b > 1) {
            r -= b - 1;
            g -= b - 1;
            b = 1;
        }

        return new float[] {r, g, b};
    }

    static int saturate(int color, float intensity) {
        float a = alpha(color) / 255F,
                red = red(color) / 255F,
                green = green(color) / 255F,
                blue = blue(color) / 255F;
        float avg = (red + green + blue) / 3F;
        float r = avg + (red - avg) * intensity,
                g = avg + (green - avg) * intensity,
                b = avg + (blue - avg) * intensity;

        if (r > 1) {
            g -= r - 1;
            b -= r - 1;
            r = 1;
        }
        if (g > 1) {
            r -= g - 1;
            b -= g - 1;
            g = 1;
        }
        if (b > 1) {
            r -= b - 1;
            g -= b - 1;
            b = 1;
        }

        return colorFromFloat(a, r, g, b);
    }
}
