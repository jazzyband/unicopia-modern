package com.minelittlepony.unicopia.client.gui.spellbook.element;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.client.gui.ParagraphWrappingVisitor;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.container.spellbook.Flow;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.vertex.PoseStack;

class TextBlock implements PageElement {
    private final List<TextBlock.Line> wrappedText = new ArrayList<>();
    private final Bounds bounds = Bounds.empty();
    private final ChapterPageElement.TextElement content;

    public TextBlock(ChapterPageElement.TextElement content) {
        this.content = content;
    }

    @Override
    public void compile(DynamicContent.GuiPage page, int y, IViewRoot container) {
        wrappedText.clear();
        ParagraphWrappingVisitor visitor = new ParagraphWrappingVisitor(
                yPosition -> page.getLineLimitAt(y + yPosition),
                (line, yPosition) -> wrappedText.add(new Line(line, page.getLeftMarginAt(y + yPosition)))
        );
        content.lines().forEach(line -> {
            line.visit(visitor, Style.EMPTY);
            visitor.advance();
        });
        visitor.forceAdvance();
        bounds.height = Minecraft.getInstance().font.lineHeight * (wrappedText.size());
    }

    @Override
    public void draw(DynamicContent.GuiPage page, GuiGraphicsExtractor context, int mouseX, int mouseY, IViewRoot container) {
        Font font = Minecraft.getInstance().font;
        boolean needsMoreXp = page.getLevel() < 0 || Pony.of(Minecraft.getInstance().player).getLevel().get() < page.getLevel();
        org.joml.Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        wrappedText.forEach(line -> {
            context.text(font, needsMoreXp ? line.text().copy().withStyle(ChatFormatting.OBFUSCATED) : line.text().copy(), line.x(), 0, page.getColor(), false);
            matrices.translate(0, font.lineHeight);
        });
        matrices.popMatrix();
    }

    @Override
    public Bounds bounds() {
        return bounds;
    }

    @Override
    public Flow flow() {
        return Flow.NONE;
    }

    private record Line(Component text, int x) { }
}
