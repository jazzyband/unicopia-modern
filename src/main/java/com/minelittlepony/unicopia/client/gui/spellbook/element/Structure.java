package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.block.state.Schematic;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public record Structure(Bounds bounds, Schematic schematic) implements PageElement {
    @Override
    public void draw(DynamicContent.GuiPage page, GuiGraphicsExtractor context, int mouseX, int mouseY, IViewRoot container) {
        if (schematic.volume() == 0) {
            return;
        }

        int centerX = container == null ? bounds.left : container.getBounds().width / 2;
        int centerY = container == null ? bounds.top : container.getBounds().height / 2;
        for (var entry : schematic.states()) {
            int x = centerX + (entry.x() - entry.z()) * 10;
            int y = centerY + (entry.x() + entry.z()) * 5 - entry.y() * 10;
            context.item(entry.state().getBlock().asItem().getDefaultInstance(), x, y);
        }
    }
}
