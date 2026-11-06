package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface SpellIconRenderer {
    static void renderSpell(GuiGraphicsExtractor context, CustomisedSpellType<?> spell, double x, double y) {
        renderSpell(context, spell, x, y, 1);
    }

    static void renderSpell(GuiGraphicsExtractor context, CustomisedSpellType<?> spell, double x, double y, float scale) {
        if (spell.isEmpty()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();

        Pony pony = Pony.of(client.player);

        if (pony.getSpellSlot().contains(spell.and(SpellPredicate.IS_VISIBLE))) {
            Matrix3x2fStack modelStack = context.pose();

            float ringScale = (0.7F + scale);

            modelStack.pushMatrix();
            modelStack.translate((float)(x + 8 * scale), (float)(y + 8 * scale));

            int color = spell.type().getColor() | 0x000000FF;
            double radius = (1.5F + Math.sin(client.player.tickCount / 9D) / 4) * ringScale;

            DrawableUtil.drawArc(context, radius, radius + 3, 0, DrawableUtil.TAU, color & 0xFFFFFF2F);
            DrawableUtil.drawArc(context, radius + 3, radius + 4, 0, DrawableUtil.TAU, color & 0xFFFFFFAF);
            pony.getSpellSlot().get(spell.and(SpellPredicate.IS_TIMED)).map(TimedSpell::getTimer).ifPresent(timer -> {
                DrawableUtil.drawArc(context, radius, radius + 3, 0, DrawableUtil.TAU * timer.getPercentTimeRemaining(client.getDeltaTracker().getGameTimeDeltaPartialTick(false)), 0xFFFFFFFF);
            });

            long count = pony.getSpellSlot().stream(spell).count();
            if (count > 1) {
                modelStack.pushMatrix();
                modelStack.translate(1, 1);
                modelStack.scale(0.8F, 0.8F);
                context.text(client.font, count > 64 ? "64+" : String.valueOf(count), 0, 0, 0xFFFFFFFF, true);
                modelStack.popMatrix();
            }

            modelStack.popMatrix();
        }

        DrawableUtil.renderItemIcon(context, spell.getDefaultStack(), x, y, scale);
    }
}
