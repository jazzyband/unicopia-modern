package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.trait.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ItemTraitsTooltipRenderer implements Component, FormattedCharSequence, ClientTooltipComponent {
    private static final Identifier UNKNOWN = Unicopia.id("textures/gui/trait/unknown.png");

    private final SpellTraits traits;

    public ItemTraitsTooltipRenderer(SpellTraits traits) {
        this.traits = traits;
    }

    @Override
    public int getHeight(Font textRenderer) {
        return getRows() * 17 + 2;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return getColumns() * 17 + 2;
    }

    private int getColumns() {
        return Math.min(traits.size(), Math.max(6, (int)Math.ceil(Math.sqrt(traits.size() + 1))));
    }

    private int getRows() {
        int columns = getColumns();
        if (columns == traits.size()) {
            return 1;
        }
        return Math.max(1, (int)Math.ceil((float)(traits.size() + 1) / getColumns()));
    }

    @Override
    public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor context) {
        int columns = getColumns();
        int i = 0;

        for (var entry : traits) {
            renderTraitIcon(entry.getKey(), entry.getValue(), context,
                    x + (i % columns) * 17,
                    y + (i / columns) * 17
            );
            i++;
        }
    }

    @Override
    public boolean accept(FormattedCharSink visitor) {
        return false;
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return this;
    }

    @Override
    public MutableComponent copy() {
        return Component.empty();
    }

    public static void renderStackTraits(ItemStack stack, GuiGraphicsExtractor context, float x, float y, float weight, float delta, int seed) {
        renderStackTraits(SpellTraits.of(stack), context, x, y, weight, delta, seed, false);
    }

    public static void renderStackTraits(SpellTraits traits, GuiGraphicsExtractor context, float x, float y, float weight, float delta, int seed, boolean revealAll) {
        float time = Mth.cos((Minecraft.getInstance().player.tickCount + delta + seed) / 2F) * 0.7F;

        float angle = 0.7F + (time / 30F) % Mth.TWO_PI;
        float angleIncrement = Mth.TWO_PI / traits.size();
        float r = 9 + 2 * Mth.sin(delta / 20F);

        for (var entry : traits) {
            if (revealAll || isKnown(entry.getKey())) {
                ItemTraitsTooltipRenderer.renderTraitIcon(entry.getKey(), entry.getValue() * weight, context,
                        x + r * Mth.sin(angle),
                        y + r * Mth.cos(angle),
                        revealAll || isKnown(entry.getKey())
                );
                angle += angleIncrement;
            }
        }
    }

    public static void renderStackSingleTrait(Trait trait, float amount, GuiGraphicsExtractor context, float x, float y, float weight, float delta, int seed, boolean revealAll) {
        float time = Mth.cos((Minecraft.getInstance().player.tickCount + delta + seed) / 2F) * 0.7F;

        float angle = 0.7F + (time / 30F) % Mth.TWO_PI;
        float r = 9 + 2 * Mth.sin(delta / 20F);

        ItemTraitsTooltipRenderer.renderTraitIcon(trait, amount * weight, context,
                x + r * Mth.sin(angle),
                y + r * Mth.cos(angle),
                revealAll || isKnown(trait)
        );
    }

    public static boolean isKnown(Trait trait) {
        return Minecraft.getInstance().player == null
            || Pony.of(Minecraft.getInstance().player).getDiscoveries().isKnown(trait);
    }

    public static void renderTraitIcon(Trait trait, float value, GuiGraphicsExtractor context, float xx, float yy) {
        renderTraitIcon(trait, value, context, xx, yy, isKnown(trait));
    }

    public static void renderTraitIcon(Trait trait, float value, GuiGraphicsExtractor context, float xx, float yy, boolean reveal) {
        Font textRenderer = Minecraft.getInstance().font;

        int size = 12;

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(xx, yy);

        context.blit(RenderPipelines.GUI_TEXTURED, reveal ? trait.getSprite() : UNKNOWN, 2, 1, 0, 0, 0, size, size, size, size);

        matrices.translate(9, 3 + size / 2);
        matrices.scale(0.5F, 0.5F);

        String count = value > 99 ? "99+" : Math.round(value) == value ? (int)value + "" : ((Math.round(value * 10) / 10F) + "");

        context.text(textRenderer, count, 0, 0, 0xFFFFFFFF, true);
        matrices.popMatrix();
    }

    @Override
    public Style getStyle() {
        return Component.empty().getStyle();
    }

    @Override
    public ComponentContents getContents() {
        return Component.empty().getContents();
    }

    @Override
    public List<Component> getSiblings() {
        return new ArrayList<>();
    }
}
