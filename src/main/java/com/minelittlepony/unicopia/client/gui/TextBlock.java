package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.Label;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public class TextBlock extends Label {
    private final int maxWidth;

    public TextBlock(int x, int y, int width) {
        super(x, y);
        this.maxWidth = width;
    }

    @Override
    public Bounds getBounds() {
        Bounds bounds = super.getBounds();
        bounds.height = getFont().split(getStyle().getText(), maxWidth).size() * getFont().lineHeight;
        bounds.width = 0;
        return bounds;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTicks) {
        int textY = (int)(getY() + Minecraft.getInstance().font.lineHeight/1.5F);

        for (FormattedCharSequence line : getFont().split(getStyle().getText(), maxWidth)) {
            context.text(getFont(), line, getX(), textY, getStyle().getColor(), true);
            textY += getFont().lineHeight;
        }
    }
}