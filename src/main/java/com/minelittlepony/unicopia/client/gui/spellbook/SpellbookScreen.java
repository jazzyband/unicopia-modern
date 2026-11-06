package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.client.gui.*;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.*;
import com.minelittlepony.unicopia.compat.trinkets.TrinketSlotBackSprites;
import com.minelittlepony.unicopia.container.*;
import com.minelittlepony.unicopia.container.inventory.*;
import com.minelittlepony.unicopia.container.spellbook.SpellbookChapter;
import com.minelittlepony.unicopia.container.spellbook.TabSide;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSpellbookStateChanged;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3x2fStack;

public class SpellbookScreen extends AbstractRecipeBookScreen<SpellbookScreenHandler> implements RecipeUpdateListener {
    public static final Identifier TEXTURE = Unicopia.id("textures/gui/container/book.png");
    public static final Identifier SLOT = Unicopia.id("textures/gui/container/slot.png");
    public static final Identifier GEM = Unicopia.id("textures/item/gemstone.png");

    private static final int CONTENT_PADDING = 30;
    private final int imageWidth = 405;
    private final int imageHeight = 219;

    public static final int TITLE_X = 30;
    public static final int TITLE_Y = 20;
    public static final int TITLE_COLOR = 0xFF404040;

    private final SpellbookTraitDexPageContent traitDex = new SpellbookTraitDexPageContent(this);
    private final SpellbookChapterList chapters = new SpellbookChapterList(this,
        new Chapter(SpellbookState.CRAFTING_ID, new SpellbookChapter(TabSide.LEFT, 0, 0, List.of()), Optional.of(new SpellbookCraftingPageContent(this))),
        new Chapter(SpellbookState.PROFILE_ID, new SpellbookChapter(TabSide.LEFT, 1, 0, List.of()), Optional.of(new SpellbookProfilePageContent(this))),
        new Chapter(SpellbookState.TRAIT_DEX_ID, new SpellbookChapter(TabSide.LEFT, 3, 0, List.of()), Optional.of(traitDex))
    );
    private final SpellbookTabBar tabs = new SpellbookTabBar(this, chapters);

    private Bounds contentBounds = Bounds.empty();

    public SpellbookScreen(SpellbookScreenHandler handler, Inventory inventory, Component title) {
        super(handler, new SpellbookRecipeBookWidget(handler), inventory, title);
        contentBounds = new Bounds(CONTENT_PADDING, CONTENT_PADDING, imageWidth - CONTENT_PADDING * 2, imageHeight - CONTENT_PADDING * 3 - 2);
        handler.getSpellbookState().setSynchronizer(state -> {
            Channel.CLIENT_SPELLBOOK_UPDATE.sendToServer(MsgSpellbookStateChanged.create(handler, state));
        });
    }

    public SpellbookState getState() {
        return menu.getSpellbookState();
    }

    public SpellbookTraitDexPageContent getTraitDex() {
        return traitDex;
    }

    public void addPageButtons(int buttonY, int prevX, int nextX, IntConsumer pageAction) {
        addRenderableWidget(new PageButton(this, leftPos + nextX, topPos + buttonY, 1, pageAction));
        addRenderableWidget(new PageButton(this, leftPos + prevX, topPos + buttonY, -1, pageAction));
    }

    public int getX() {
        return leftPos;
    }

    public int getY() {
        return topPos;
    }

    public int getBackgroundWidth() {
        return imageWidth;
    }

    public int getBackgroundHeight() {
        return imageHeight;
    }

    public Bounds getFrameBounds() {
        return contentBounds;
    }

    @Override
    protected ScreenPosition getRecipeBookButtonPosition() {
        return new ScreenPosition(0, 0);
    }

    @Override
    public <T extends Renderable> T addRenderableOnly(T drawable) {
        return super.addRenderableOnly(drawable);
    }

    @Override
    public void init() {
        super.init();
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
        tabs.init();
        chapters.getCurrentChapter().content().ifPresent(content -> content.init(this, chapters.getCurrentChapter().id()));
    }

    @Override
    public void recipesUpdated() {
        super.recipesUpdated();
        chapters.getCurrentChapter()
            .content()
            .map(i -> i instanceof RecipesChangedListener ? (RecipesChangedListener)i : null)
            .ifPresent(RecipesChangedListener::onRecipesChanged);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTicks) {
        if (getState().isDirty()) {
            rebuildWidgets();
        }
        super.extractRenderState(context, mouseX, mouseY, partialTicks);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {

        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 512, 256);

        tabs.getAllTabs().forEach(tab -> {
            Bounds bounds = tab.bounds();
            boolean hover = bounds.contains(mouseX, mouseY);

            int color = tab.chapter().details().color() & 0xFFFFFF;

            int v = 100 + (hover ? 24 : 0);
            if (color == 0xFFFFFF || color == 0) {
                v += 48;
            } else {
            }

            boolean isRight = tab.chapter().details().side() == TabSide.RIGHT;

            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, bounds.left, bounds.top, isRight ? 510 - bounds.width : 402, v, bounds.width, bounds.height, 512, 256);

            float widthScale = bounds.width / 35F;
            int resolution = (int)(16 * widthScale);
            int iconInset = (int)(Math.min(widthScale * 2, 1) * 10);

            context.blit(RenderPipelines.GUI_TEXTURED, tab.icon().get(),
                    isRight ? bounds.left + bounds.width - resolution - iconInset : bounds.left + iconInset,
                    bounds.top + (bounds.height - resolution) / 2,
                    0, 0,
                    resolution, resolution,
                    resolution, resolution
            );

            if (bounds.contains(mouseX, mouseY)) {
                context.setTooltipForNextFrame(Component.translatable(Util.makeDescriptionId("chapter", tab.chapter().id())), mouseX, mouseY);
            }
        });

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(leftPos, topPos);
        chapters.getCurrentChapter().content().ifPresent(content -> content.draw(context, mouseX, mouseY, this));
        matrices.popMatrix();
        super.extractContents(context, mouseX, mouseY, delta);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor context, int x, int y) {

        if (!(hoveredSlot instanceof SpellSlot sp)) {
            super.extractTooltip(context, x, y);
            return;
        }

        CustomisedSpellType<?> spell = sp.getSpell();
        if (spell.isEmpty()) {
            context.setTooltipForNextFrame(font, Component.translatable("gui.unicopia.spellbook.empty_spell_slot"), x, y);
            return;
        }

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(spell.type().getName());
        spell.addToTooltip(TooltipContext.of(minecraft.level), tooltip::add, TooltipFlag.NORMAL, DataComponentMap.EMPTY);

        context.setComponentTooltipForNextFrame(font, tooltip, x, y);
    }

    void drawSlots(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(leftPos, topPos);

        for (Slot slot : menu.slots) {
            if (slot.isActive() && slot instanceof SpellbookSlot p) {
                context.blit(RenderPipelines.GUI_TEXTURED, SLOT, slot.x - 8, slot.y - 8, 0, 0, 32, 32, 32, 32);

                if (slot.getItem().isEmpty()) {
                    Identifier foreground = p.getForegroundIdentifier();
                    if (foreground != null) {
                        if (p.isTrinket()) {
                            foreground = TrinketSlotBackSprites.getBackSprite(foreground);
                        }
                        context.blit(RenderPipelines.GUI_TEXTURED, foreground, slot.x, slot.y, 0, 0, 16, 16, 16, 16, ARGB.color(ARGB.as8BitChannel(p.getBackSpriteOpacity()), CommonColors.WHITE));
                    }
                }

                if (slot instanceof SpellSlot sp) {
                    CustomisedSpellType<?> spell = sp.getSpell();

                    if (!spell.isEmpty()) {
                        context.pose().pushMatrix();
                        SpellIconRenderer.renderSpell(context, spell, slot.x, slot.y, 0.5F);
                        context.pose().popMatrix();
                    }
                }

                if (p.showTraits()) {
                    float weight = p.getWeight();
                    ItemTraitsTooltipRenderer.renderStackTraits(slot.getItem(), context, slot.x, slot.y, weight == 0 ? 1 : weight, delta, slot.index);
                }
            }
        }

        matrices.popMatrix();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        return tabs.getAllTabs().anyMatch(tab -> {
            if (tab.bounds().contains(mouseX, mouseY) && chapters.getCurrentChapter() != tab.chapter()) {
                getState().setCurrentPageId(tab.chapter().id());
                GameGui.playSound(USounds.Vanilla.BOOK_PAGE_TURN);
                rebuildWidgets();
                return true;
            }

            return false;
        }) || super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void rebuildWidgets() {
        super.rebuildWidgets();
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return getChildAt(event.x(), event.y()).filter((element) -> {
            setDragging(false);
            return element.mouseReleased(event);
        }).isPresent() || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        return (getFocused() != null && isDragging() && event.button() == 0 && getFocused().mouseDragged(event, deltaX, deltaY))
            || super.mouseDragged(event, deltaX, deltaY);
    }

    private static class PageButton extends ImageButton {
        private final int increment;
        private final TextureSprite sprite = new TextureSprite()
                .setSize(25, 13)
                .setTextureSize(512, 256)
                .setTextureOffset(0, 479)
                .setTexture(TEXTURE);

        public PageButton(SpellbookScreen screen, int x, int y, int increment, IntConsumer pageAction) {
            super(x, y, 25, 20);
            this.increment = increment;
            getStyle().setIcon(sprite);
            onClick(sender -> {
                pageAction.accept(increment);
                screen.rebuildWidgets();
            });
        }

        @Override
        public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float tickDelta) {
            if (!active) {
               return;
            }

            int state = isHovered ? 1 : 0;

            sprite.setTextureOffset(23 * state, (int)(479 + 6.5F - (increment * 6.5F)));
            super.extractContents(context, mouseX, mouseY, tickDelta);
        }
    }

    static class ImageButton extends Button {

        public ImageButton(int x, int y) {
            super(x, y);
        }

        public ImageButton(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float tickDelta) {

            if (getStyle().hasIcon()) {
                getStyle().getIcon().render(context, getX(), getY(), mouseX, mouseY, tickDelta);
            }

        }
    }

    public interface RecipesChangedListener {
        void onRecipesChanged();
    }
}
