package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.sprite.ISprite;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class TribeButton extends Button {

    private final int screenWidth;
    private final Race race;

    public TribeButton(int x, int y, int screenWidth, Race race) {
        super(x, y, 70, 70);
        this.screenWidth = screenWidth;
        this.race = race;

        getStyle().setIcon(createSprite(race, (70 - 32) / 2, 0, 32)).setText(race.getTranslationKey());
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, getX()  - 3, getY() - 13, 0, 0, 76, 69, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);
        if (isHoveredOrFocused()) {
            context.blit(RenderPipelines.GUI_TEXTURED, TribeSelectionScreen.TEXTURE, getX()  - 4, getY() - 14, 76, 0, 78, 71, TribeSelectionScreen.TEXTURE_SIZE, TribeSelectionScreen.TEXTURE_SIZE);

            if (isFocused() && screenWidth > 0) {
                Identifier id = Race.REGISTRY.getKey(race);
                context.centeredText(getFont(), Component.translatable("gui.unicopia.tribe_selection.describe." + id.getNamespace() + "." + id.getPath()), screenWidth / 2, getY() + height, 0xFFFFFFFF);
            }
        }

        ISprite icon = getStyle().getIcon();

        if (getStyle().hasIcon()) {
            icon.render(context, getX(), getY(), mouseX, mouseY, partialTicks);
        }

        int originalColor = getStyle().getColor();
        int foreColor = originalColor;
        if (!active) {
            foreColor = 10526880;
        } else if (isHoveredOrFocused()) {
            foreColor = 16777120;
        }

        setMessage(getStyle().getText());

        getStyle().setIcon(ISprite.EMPTY);

        getStyle().setColor(foreColor | Mth.ceil(alpha * 255.0F) << 24);
        renderForeground(context, context.textRenderer(), mouseX, mouseY);
        getStyle().setColor(originalColor);

        getStyle().setIcon(icon);
    }

    public static ISprite createSprite(Race race, int x, int y, int size) {
        return new TextureSprite()
            .setPosition(x, y)
            .setSize(size, size)
            .setTextureSize(size, size)
            .setTexture(race.getIcon());
    }
}
