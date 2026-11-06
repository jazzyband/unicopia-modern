package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.platform.InputConstants;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Label;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRequestSpeciesChange;

public class TribeSelectionScreen extends GameGui implements HidesHud {
    static final Identifier TEXTURE = Unicopia.id("textures/gui/tribe_selection.png");
    static final int TEXTURE_SIZE = 256;

    final Set<Race> allowedRaces;

    final Component journeyText;
    final Component choiceText;

    private boolean finished;

    private final List<TribeButton> options = new ArrayList<>();
    private static int SELECTION = -1;

    private int prevScrollPosition;
    private int scrollPosition;
    private int targetScroll;

    public TribeSelectionScreen(Set<Race> allowedRaces, String baseString) {
        super(Component.translatable(baseString));
        this.allowedRaces = allowedRaces;
        this.journeyText = Component.translatable(baseString + ".journey");
        this.choiceText = Component.translatable(baseString + ".choice");
    }

    @Override
    protected void init() {
        final int pageWidth = 300;
        final int left = (width - pageWidth) / 2;

        int top = 0;

        addRenderableOnly(new Label(width / 2, top += 20).setCentered()).getStyle().setText(getTitle().copy().withStyle(ChatFormatting.YELLOW));

        top += height / 8;

        TextBlock block = addRenderableOnly(new TextBlock(left, top += 10, pageWidth));
        block.getStyle().setText(journeyText);
        top += block.getBounds().height;

        block = addRenderableOnly(new TextBlock(left, top += 7, pageWidth));
        block.getStyle().setText(choiceText);
        top += block.getBounds().height;
        top += 30;

        List<Race> options = Race.REGISTRY.stream().filter(race -> race.availability().isSelectable()).toList();
        this.options.clear();

        for (Race race : options) {
            if (race.isHuman()) {
                final Race r = race;
                addButton(new Link(width / 2, height - getFont().lineHeight - 10).setCentered())
                    .onClick(s -> selectRace(r))
                    .getStyle().setText("[Or Play as None of these]");
            } else {
                addOption(race, top);
            }
        }

        scroll(SELECTION == -1 ? options.size() / 2 : SELECTION, false);
    }

    private void addOption(Race race, int y) {
        var option = new TribeButton(0, y, width, race);
        int index = options.size();
        options.add(addRenderableWidget(option));
        option.onClick(b -> {
            scroll(index, false);
            selectRace(race);
        }).setEnabled(allowedRaces.contains(race));
    }

    private void selectRace(Race race) {
        finished = true;
        minecraft.gui.setScreen(new TribeConfirmationScreen(result -> {
            finished = false;

            if (result) {
                Channel.CLIENT_REQUEST_SPECIES_CHANGE.sendToServer(new MsgRequestSpeciesChange(true, race));
                finish();
            } else {
                minecraft.gui.setScreen(this);
            }
        }, race));
    }

    @Override
    public void tick() {
        prevScrollPosition = scrollPosition;
        if (scrollPosition < targetScroll) {
            scrollPosition++;
        }
        if (scrollPosition > targetScroll) {
            scrollPosition--;
        }
    }

    private void updateScolling() {
        final int itemWidth = 70 + 10;
        int x = (width - itemWidth) / 2;
        float diff = Mth.lerpInt(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false), prevScrollPosition, scrollPosition) / 4F;

        for (int i = 0; i < options.size(); i++) {
            var option = options.get(i);
            option.setX((int)(x + (i - diff) * itemWidth));
            option.setFocused(i == SELECTION);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        updateScolling();
        super.extractRenderState(context, mouseX, mouseY, delta);

        if (!options.isEmpty()) {
            var element = options.get(0);

            float diff = (targetScroll - Mth.lerpInt(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false), prevScrollPosition, scrollPosition)) * 7;
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, (width / 2) + 40 + (scrollPosition < targetScroll ? (int)diff : 0), element.getY() - 20, 10, 165, 153, 30, 85, 312, 312);
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, (width / 2) - 80 + (scrollPosition > targetScroll ? (int)diff : 0), element.getY() - 20, 10, 195, 153, 30, 85, 312, 312);
            if (element.getBounds().left < 0) {
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 20, element.getY() - 10, 10, 188, 235, 24, 60, 312, 312);
            }
            element = options.get(options.size() - 1);
            if (element.getBounds().right() > width) {
                context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, width - 50, element.getY() - 10, 10, 164, 235, 24, 60, 312, 312);
            }
        }
    }

    @Override
    public void finish() {
        finished = true;
        onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (keyCode == InputConstants.KEY_LEFT) {
            scroll(Math.max(SELECTION - 1, 0), true);
            return true;
        }
        if (keyCode == InputConstants.KEY_RIGHT) {
            scroll(Math.min(SELECTION + 1, options.size() - 1), true);
            return true;
        }
        if (keyCode == InputConstants.KEY_RETURN) {
            if (options.get(SELECTION).active) {
                options.get(SELECTION).onPress(event);
            }
        }

        return false;
    }

    private void scroll(int target, boolean animate) {
        SELECTION = target;
        target *= 4;
        if (targetScroll == target) {
            return;
        }
        targetScroll = target;
        if (!animate) {
            scrollPosition = targetScroll;
            prevScrollPosition = scrollPosition;
        } else {
            playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void removed() {
        if (!finished && minecraft != null) {
            minecraft.execute(() -> {
                finished = true;
                minecraft.gui.setScreen(this);
                finished = false;
            });
        }
    }

    static class Link extends Label implements LayoutElement {
        public Link(int x, int y) {
            super(x, y);
        }

        @Override
        protected boolean isValidClickButton(MouseButtonInfo button) {
            return button.button() == 0;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return getBounds().contains(mouseX, mouseY);
        }

        @Override
        public ScreenRectangle getRectangle() {
            return new ScreenRectangle(getX(), getY(), getWidth(), getHeight());
        }

        @Override
        public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTicks) {
            this.isHovered = isMouseOver(mouseX, mouseY);
            getStyle().setColor(isHoveredOrFocused() ? CommonColors.LIGHTER_GRAY : CommonColors.GRAY);
            super.extractContents(context, mouseX, mouseY, partialTicks);
        }
    }
}
