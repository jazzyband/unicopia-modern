package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.client.gui.*;
import com.minelittlepony.unicopia.entity.player.*;
import org.joml.Matrix3x2fStack;
import com.sollace.romanizer.api.Romanizer;

public class SpellbookProfilePageContent implements SpellbookChapterList.Content {
    private static final Component MANA_LABEL = Component.translatable("gui.unicopia.spellbook.page.mana");
    private final Minecraft client = Minecraft.getInstance();
    private final Pony pony = Pony.of(client.player);
    private final Font font = client.font;

    private final SpellbookScreen screen;

    public SpellbookProfilePageContent(SpellbookScreen screen) {
        this.screen = screen;
    }

    @Override
    public void init(SpellbookScreen screen, Identifier pageId) {
        Bounds bounds = screen.getFrameBounds();
        int size = 32;
        int x = screen.getX() + bounds.left + bounds.width / 4 - size + 5;
        int y = screen.getY() + bounds.top + bounds.height / 2 + 3;

        screen.addRenderableOnly(new SpellbookScreen.ImageButton(x, y, size, size))
            .getStyle()
                .setIcon(TribeButton.createSprite(pony.getSpecies(), 0, 0, size))
                .setTooltip(() -> List.of(
                        Component.literal(String.format("Level %d ", pony.getLevel().get() + 1)).append(pony.getSpecies().getDisplayName()).withStyle(pony.getSpecies().getAffinity().getColor()),
                        Component.literal(String.format("Mana: %d%%", (int)(pony.getMagicalReserves().getMana().getPercentFill() * 100))),
                        Component.literal(String.format("Corruption: %s%d%%", pony.getCorruptionhandler().hasCorruptingMagic() ? "^" : "", (int)(pony.getCorruption().getScaled(100)))),
                        Component.literal(String.format("Experience: %d", (int)(pony.getMagicalReserves().getXp().getPercentFill() * 100))),
                        Component.literal(String.format("Next level in: %dxp", 100 - (int)(pony.getMagicalReserves().getXp().getPercentFill() * 100)))
                ));

        Race inherited = pony.getCompositeRace().collapsed();
        if (inherited != pony.getSpecies()) {
            int halfSize = size / 2;
            screen.addRenderableOnly(new SpellbookScreen.ImageButton(x + halfSize, y + halfSize, halfSize, halfSize))
                .getStyle()
                    .setIcon(TribeButton.createSprite(inherited, 0, 0, halfSize));
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor context, int mouseX, int mouseY, IViewRoot container) {

        int y = SpellbookScreen.TITLE_Y;

        float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float delta = pony.asEntity().tickCount + tickDelta;
        int currentLevel = pony.getLevel().get();
        float currentScaledLevel = pony.getLevel().getScaled(1);
        float currentCorruption = pony.getCorruption().getScaled(1);

        DrawableUtil.drawScaledText(context, pony.asEntity().getName(), SpellbookScreen.TITLE_X, y, 1.3F, MagicText.getColor());
        DrawableUtil.drawScaledText(context, ExperienceGroup.forLevel(
                currentScaledLevel,
                currentCorruption
        ), SpellbookScreen.TITLE_X, y + 13, 0.8F,
                ARGB.srgbLerp(currentCorruption,
                        ARGB.srgbLerp(currentScaledLevel, 0xAA0040FF, 0xAAA0AA40),
                        0xAAFF0000
                )
        );

        MagicReserves reserves = pony.getMagicalReserves();

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(screen.getBackgroundWidth() / 2 + SpellbookScreen.TITLE_X - 10, y);
        matrices.scale(1.3F, 1.3F);
        context.text(font, SpellbookCraftingPageContent.INVENTORY_TITLE, 0, 0, MagicText.getColor(), false);
        matrices.popMatrix();

        Bounds bounds = screen.getFrameBounds();

        matrices.pushMatrix();
        matrices.translate(bounds.left + bounds.width / 4 - 10, bounds.top + bounds.height / 2 + 20);

        double growth = Mth.sin(delta / 9F) * 2;

        double radius = 40 + growth;
        float xpPercentage = reserves.getXp().getPercentFill(tickDelta);
        float manaPercentage = reserves.getMana().getPercentFill(tickDelta);

        float alphaF = (Mth.sin(delta / 9F) + 1) / 2F;
        int alpha = (int)(alphaF * 0x10) & 0xFF;
        int color = 0x10404000 | alpha;
        int xpColor = 0xAA0040FF | ((int)((0.3F + 0.7F * xpPercentage) * 0xFF) & 0xFF) << 16;
        int manaColor = 0xFF00F040;
        if (pony.getSpellSlot().get(SpellPredicate.IS_CORRUPTING).isPresent()) {
            manaColor = ARGB.srgbLerp(Math.abs(Mth.sin(pony.asEntity().tickCount / 15F)), manaColor, 0xFF0030F0);
        }
        manaColor |= (int)((0.3F + 0.7F * alphaF) * 0x40) << 16;

        DrawableUtil.drawArc(context, 0, radius + 24, 0, DrawableUtil.TAU, color);
        DrawableUtil.drawArc(context, radius / 3, radius + 6, 0, DrawableUtil.TAU, color);

        if (currentLevel >= pony.getLevel().getMax()) {
            int rayCount = 6;
            float raySeparation = Mth.TWO_PI / rayCount;
            float rotate = (delta / 120) % (Mth.TWO_PI / (rayCount / 2));

            growth = Mth.sin(delta / 10F) * 2;

            int bandAColor = ARGB.srgbLerp(currentCorruption, 0xAAFFAA60, 0xFF000030);
            int bandBColor = ARGB.srgbLerp(currentCorruption, 0xFFFFFF40, 0x00000020);

            float glowSize = Mth.lerp(currentCorruption, 8, -8);

            for (int i = 0; i < rayCount; i++) {
                double rad = (radius + glowSize) * 0.8F + growth - (i % 2) * 5;
                float rot = (rotate + raySeparation * i) % Mth.TWO_PI;

                DrawableUtil.drawArc(context, 0, rad, rot, 0.2F, bandAColor);
                DrawableUtil.drawArc(context, 0, rad + 0.3F, rot + 0.37F, 0.25F, bandBColor);
            }
        }

        DrawableUtil.drawArc(context, radius / 3, radius + 6, 0, xpPercentage * DrawableUtil.TAU, xpColor);
        radius += 8;
        DrawableUtil.drawArc(context, radius, radius + 6 + growth, 0, manaPercentage * DrawableUtil.TAU, manaColor);

        String manaString = (int)reserves.getMana().get() + "/" + (int)reserves.getMana().getMax();

        y = 15;

        context.text(font, MANA_LABEL, -font.width(MANA_LABEL) / 2, y, SpellbookScreen.TITLE_COLOR, false);
        context.text(font, manaString, -font.width(manaString) / 2, y += font.lineHeight, SpellbookScreen.TITLE_COLOR, false);

        Component levelString = Component.literal(Romanizer.romanize(currentLevel + 1));

        matrices.translate(-font.width(levelString), -35);
        matrices.scale(2F, 2F);
        context.text(font, levelString, 0, 0, SpellbookScreen.TITLE_COLOR, false);
        matrices.popMatrix();

        matrices.pushMatrix();
        matrices.translate(-screen.getX(), -screen.getY());
        screen.drawSlots(context, mouseX, mouseY, 0);
        matrices.popMatrix();
    }

    static void drawBar(GuiGraphicsExtractor context, int x, int y, float value, int color) {
        int barWidth = 40;
        int midpoint = x + (int)(barWidth * value);
        context.fill(x, y, midpoint, y + 5, 0xFFAAFFFF);
        context.fill(midpoint, y, x + barWidth, y + 5, color);
    }
}
