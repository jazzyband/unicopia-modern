package com.minelittlepony.unicopia.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public interface MagicText {
    static int getColor() {
        Minecraft client = Minecraft.getInstance();
        float ticks = client.player.tickCount + client.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        float sin = (Mth.sin(ticks / 10F) + 1) * 155 * 0.25F;
        float cos = (Mth.cos((ticks + 10) / 10F) + 1) * 155 * 0.25F;

        return (int)(sin + cos);
    }
}
