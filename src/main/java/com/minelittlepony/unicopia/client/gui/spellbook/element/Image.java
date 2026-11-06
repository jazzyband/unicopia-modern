package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.container.spellbook.Flow;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;

record Image(ChapterPageElement.Image image) implements PageElement {
    @Override
    public void draw(DynamicContent.GuiPage page, GuiGraphicsExtractor context, int mouseX, int mouseY, IViewRoot container) {
        context.blit(RenderPipelines.GUI_TEXTURED, image().texture(), 0, 0, 0, 0, 0, bounds().width, bounds().height, bounds().width, bounds().height);
    }

    @Override
    public Flow flow() {
        return image.flow();
    }

    @Override
    public Bounds bounds() {
        return image.bounds();
    }
}
