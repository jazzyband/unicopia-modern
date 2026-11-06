package com.minelittlepony.unicopia.client.gui;

import java.util.*;
import java.util.function.BiConsumer;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText.StyledContentConsumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.minecraft.network.chat.FormattedText;

public class ParagraphWrappingVisitor implements StyledContentConsumer<Object> {
    private int line = 0;
    private int pageWidth;

    private final Font font = Minecraft.getInstance().font;
    private final StringSplitter handler = font.getSplitter();

    private float currentLineCollectedLength = 0;
    private MutableComponent currentLine = Component.empty();
    private boolean progressedNonEmpty;

    private final Int2IntFunction widthSupplier;
    private final BiConsumer<Component, Integer> lineConsumer;

    public ParagraphWrappingVisitor(Int2IntFunction widthSupplier, BiConsumer<Component, Integer> lineConsumer) {
        this.widthSupplier = widthSupplier;
        this.lineConsumer = lineConsumer;
        pageWidth = widthSupplier.applyAsInt((line) * font.lineHeight);
    }

    @Override
    public Optional<Object> accept(Style initialStyle, String s) {
        StyleBreakingVisitor visitor = new StyleBreakingVisitor(initialStyle, this::acceptFragment);
        StringDecomposer.iterateFormatted(s, 0, initialStyle, initialStyle, visitor);
        visitor.flush();
        return Optional.empty();
    }

    private Optional<Object> acceptFragment(Style inlineStyle, String s) {
        int remainingLength = (int)(pageWidth - currentLineCollectedLength);

        while (!s.isEmpty()) {
            int trimmedLength = handler.plainIndexAtWidth(s, remainingLength, inlineStyle);
            int newline = s.indexOf('\n');

            if (newline >= 0 && newline < trimmedLength) {
                trimmedLength = newline + 1;
            } else {
                newline = -1;
            }

            if (trimmedLength == 0) {
                trimmedLength = s.length();
            }

            // avoid breaking in the middle of a word
            if (trimmedLength < s.length() - 1 && trimmedLength > 0
                    && (!Character.isWhitespace(s.charAt(trimmedLength + 1)) || !Character.isWhitespace(s.charAt(trimmedLength - 1)))) {
                String wrappedFragment = s.substring(0, trimmedLength);
                int lastSpace = wrappedFragment.lastIndexOf(' ');
                trimmedLength = lastSpace > 0 ? Math.min(lastSpace, trimmedLength) : trimmedLength;
            }

            Component fragment = Component.literal(s.substring(0, trimmedLength).trim()).setStyle(inlineStyle);
            float grabbedWidth = handler.stringWidth(fragment);

            // advance if appending the next segment would cause an overflow
            if (currentLineCollectedLength + grabbedWidth > pageWidth) {
                advance();
            }

            // append the segment to the line that's being built
            if (currentLineCollectedLength > 0 && s.startsWith(" ")) {
                currentLine.append(" ");
                currentLineCollectedLength += handler.stringWidth(" ");
            }
            currentLine.append(fragment);
            currentLineCollectedLength += grabbedWidth;

            // append the segment to the line that's being built
            if (currentLineCollectedLength > 0 && s.endsWith(" ")) {
                currentLine.append(" ");
                currentLineCollectedLength += handler.stringWidth(" ");
            }

            if (newline >= 0) {
                advance();
            }

            if (trimmedLength <= s.length()) {
                s = s.substring(trimmedLength, s.length());
            }

            remainingLength = pageWidth;
        }

        return Optional.empty();
    }

    public void forceAdvance() {
        if (currentLineCollectedLength > 0) {
            advance();
        }
        advance();
    }

    public void advance() {
        line++;
        if (progressedNonEmpty || currentLineCollectedLength > 0) {
            progressedNonEmpty = true;
            lineConsumer.accept(currentLine, line * font.lineHeight);
        }
        pageWidth = widthSupplier.applyAsInt(line * font.lineHeight);
        currentLine = Component.empty();
        currentLineCollectedLength = 0;
    }

    static final class StyleBreakingVisitor implements FormattedCharSink {
        private final StyledContentConsumer<?> fragmentVisitor;
        private final StringBuilder collectedText = new StringBuilder();

        private Style currentStyle;

        public StyleBreakingVisitor(Style initialStyle, StyledContentConsumer<?> fragmentVisitor) {
            this.currentStyle = initialStyle;
            this.fragmentVisitor = fragmentVisitor;
        }

        @Override
        public boolean accept(int index, Style style, int codepoint) {
            if (!style.equals(currentStyle)) {
                flush();
            }
            currentStyle = style;
            collectedText.append((char)codepoint);
            return true;
        }

        public void flush() {
            if (collectedText.length() > 0) {
                fragmentVisitor.accept(currentStyle, collectedText.toString());
                collectedText.setLength(0);
            }
        }
    }
}