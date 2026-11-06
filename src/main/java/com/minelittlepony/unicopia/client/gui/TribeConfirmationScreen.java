package com.minelittlepony.unicopia.client.gui;

import com.mojang.blaze3d.platform.InputConstants;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.ScrollContainer;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.Race;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class TribeConfirmationScreen extends GameGui implements HidesHud {
    private final Race selection;

    private final BooleanConsumer callback;

    private final ScrollContainer textBody = new ScrollContainer();

    public TribeConfirmationScreen(BooleanConsumer callback, Race selection) {
        super(Component.translatable("gui.unicopia.tribe_selection"));
        this.callback = callback;
        this.selection = selection;
    }

    @Override
    protected void init() {
        if (parent != null) {
            parent.init(width, height);
        }
        final int columnHeight = 167;
        final int columnWidth = 310;
        final int padding = 15;

        int top = (height - columnHeight) / 2;
        int left = (width - columnWidth) / 2 + 8;
        int maxWidth = 295;

        textBody.verticalScrollbar.layoutToEnd = true;
        textBody.margin.top = top + 43;
        textBody.margin.left = left;
        textBody.margin.right = width - (left + maxWidth);
        textBody.margin.bottom = height - (textBody.margin.top + 130);
        textBody.getContentPadding().top = 10;
        textBody.getContentPadding().left = 8;
        textBody.getContentPadding().bottom = 100;
        textBody.getContentPadding().right = 0;
        textBody.init(this::buildTextBody);

        getChildElements().add(textBody);

        addRenderableWidget(new Button(width / 2 + 5, top + columnHeight + padding, 100, 20))
            .onClick(b -> callback.accept(true))
            .getStyle().setText(Component.translatable("gui.unicopia.tribe_selection.join"));
        addRenderableWidget(new Button(width / 2 - 105, top + columnHeight + padding, 100, 20))
            .onClick(b -> callback.accept(false))
            .getStyle().setText(Component.translatable("gui.unicopia.tribe_selection.cancel"));

        addRenderableOnly(new Label(width / 2, top - 30).setCentered()).getStyle().setText(Component.translatable("gui.unicopia.tribe_selection.confirm", selection.getDisplayName().copy().withStyle(ChatFormatting.YELLOW)));

        addRenderableOnly(new TribeButton((width - 70) / 2, top, 0, selection));
    }

    private void buildTextBody() {
        int top = 0;

        int left = 0;

        Component race = selection.getAltDisplayName().copy().withStyle(ChatFormatting.YELLOW);

        textBody.addButton(new Label(left - 3, top += 10)).getStyle().setText(Component.translatable("gui.unicopia.tribe_selection.confirm.goods", race).withStyle(ChatFormatting.YELLOW));

        top += 15;

        int maxWidth = 270;

        Identifier id = Race.REGISTRY.getKey(selection);

        for (int i = 0; i < 10; i++) {
            String key = String.format("gui.unicopia.tribe_selection.confirm.goods.%d.%s.%s", i, id.getNamespace(), id.getPath());
            if (Language.getInstance().has(key)) {
                TextBlock block = textBody.addButton(new TextBlock(left, top, maxWidth));
                block.getStyle().setText(Component.translatable(key));
                top += block.getBounds().height;
            }
        }

        textBody.addButton(new Label(left - 3, top += 5)).getStyle().setText(Component.translatable("gui.unicopia.tribe_selection.confirm.bads", race).withStyle(ChatFormatting.YELLOW));

        top += 15;

        for (int i = 0; i < 10; i++) {
            String key = String.format("gui.unicopia.tribe_selection.confirm.bads.%d.%s.%s", i, id.getNamespace(), id.getPath());
            if (Language.getInstance().has(key)) {
                TextBlock block = textBody.addButton(new TextBlock(left, top, maxWidth));
                block.getStyle().setText(Component.translatable(key));
                top += block.getBounds().height;
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.pose().pushMatrix();
        if (parent != null) {
            parent.extractRenderState(context, -1, -1, delta);
        }
        extractTransparentBackground(context);

        final int columnHeight = 180;
        final int columnWidth = 310;
        final int segmentWidth = 123;

        int top = (height - columnHeight) / 2;
        int left = (width - columnWidth) / 2;

        top += 40;
        final int zOffset = 0;

        context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, left + zOffset, top, 0, 70, 123, columnHeight, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);
        context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, left + segmentWidth + zOffset, top, 20, 70, 123, columnHeight, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);
        context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, width - left - segmentWidth + zOffset + 1, top, 10, 70, 123, columnHeight, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);
        top -= 31;
        left = width / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, left - 55, top, 140, 70, 21, 50, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);
        context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, left + 35, top, 148, 70, 21, 50, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);
        textBody.extractRenderState(context, mouseX, mouseY, delta);
        context.pose().popMatrix();
        context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, left - 35, top - 5, 10, 70, 69, 50, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);
        context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, left - 35, top - 15, 10, 70, 69, 50, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) {
            callback.accept(false);
            return true;
        }
        if (event.key() == InputConstants.KEY_RETURN) {
            callback.accept(true);
            return true;
        }
        return super.keyPressed(event);
    }
}
