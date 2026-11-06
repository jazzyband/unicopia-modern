package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector4f;

import com.google.common.base.MoreObjects;
import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRemoveSpell;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.input.MouseButtonEvent;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.Item;

public class DismissSpellScreen extends GameGui {
    private final Pony pony = Pony.of(Minecraft.getInstance().player);

    private int relativeMouseX;
    private int relativeMouseY;

    public DismissSpellScreen() {
        super(Component.translatable("gui.unicopia.dismiss_spell"));
    }

    @Override
    protected void init() {
        double azimuth = 0;
        double ring = 2;

        List<PlacementControlSpell> placeableSpells = new ArrayList<>();

        for (Spell spell : pony.getSpellSlot().stream().filter(SpellPredicate.IS_VISIBLE).toList()) {

            if (spell instanceof PlacementControlSpell placeable) {
                if (placeable.getPosition().isPresent()) {
                    placeableSpells.add(placeable);
                    continue;
                }
            }

            addRenderableWidget(new Entry(spell).ofRadial(75 * ring - 25, azimuth));
            ring *= 1 + 0.03 / ring;
            azimuth += Math.PI / (8 * ring);
        }

        double minimalDistance = 75 * (ring - 1) - 25;
        Vec3 origin = pony.asEntity().position();

        placeableSpells.forEach(placeable -> {
            placeable.getPosition().ifPresent(position -> {
                Vec3 relativePos = position.subtract(origin).multiply(1, 0, 1);
                float yaw = minecraft.gameRenderer.mainCamera().yaw();
                Vec3 cartesian = relativePos
                        .normalize()
                        .scale(minimalDistance + relativePos.horizontalDistance())
                        .yRot((180 + yaw) * Mth.DEG_TO_RAD);
                addRenderableWidget(new Entry(placeable).ofCartesian(cartesian));
            });
        });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, width, height / 2, 0xF0101010, 0x80101010);
        context.fillGradient(0, height / 2, width, height, 0x80101010, 0xF0101010);

        relativeMouseX = -width + mouseX * 2;
        relativeMouseY = -height + mouseY * 2;

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(width - mouseX, height - mouseY);


        super.extractRenderState(context, mouseX, mouseY, delta);

        DrawableUtil.drawLine(context, 0, 0, relativeMouseX, relativeMouseY, 0xFFFFFF88);
        DrawableUtil.renderRaceIcon(context, pony.getObservedSpecies(), 0, 0, 16);
        matrices.popMatrix();

        DrawableUtil.drawLine(context, mouseX, mouseY - 4, mouseX, mouseY + 4, 0xFFAAFF99);
        DrawableUtil.drawLine(context, mouseX - 4, mouseY, mouseX + 4, mouseY, 0xFFAAFF99);

        matrices.pushMatrix();
        Component cancel = Component.translatable("gui.unicopia.dispell_screen.cancel");
        context.text(getFont(), cancel, (width - getFont().width(cancel)) / 2, height - 30, 0xFFFFFFFF, true);
        matrices.popMatrix();
    }

    @Override
    public void extractTransparentBackground(GuiGraphicsExtractor context) {

    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor context) {
        DrawableUtil.drawArc(context, 40, 80, 0, DrawableUtil.TAU, 0x00000010);
        DrawableUtil.drawArc(context, 160, 1600, 0, DrawableUtil.TAU, 0x00000020);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    class Entry extends Vector4f implements GuiEventListener, Renderable, NarratableEntry {

        private final Spell spell;
        private final Spell actualSpell;

        private boolean lastMouseOver;

        private final Vector4f copy = new Vector4f();

        public Entry(Spell spell) {
            this.spell = spell;
            this.actualSpell = getActualSpell();
        }

        public Entry ofRadial(double radius, double azimuth) {
            SphereModel.convertToCartesianCoord(this, radius, azimuth, azimuth);
            add(0,  -(float)radius / 2F, 0, 0);
            return this;
        }

        public Entry ofCartesian(Vec3 pos) {
            add((float)pos.x, (float)pos.z, (float)pos.y, 1);
            return this;
        }

        private Spell getActualSpell() {
            return spell instanceof AbstractDelegatingSpell s ? MoreObjects.firstNonNull(s.getDelegate(), s)
                : spell instanceof PlacementControlSpell s ? MoreObjects.firstNonNull(s.getDelegate(), s)
                : spell;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (isMouseOver(relativeMouseX, relativeMouseY)) {
                removeWidget(this);
                pony.getSpellSlot().removeIf(spell -> spell == this.spell);
                Channel.REMOVE_SPELL.sendToServer(new MsgRemoveSpell(spell));
                playClickEffect();
                return true;
            }
            return false;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return squareDistance(mouseX, mouseY, x, y) < 75;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float tickDelta) {
            Matrix3x2fStack matrices = context.pose();

            var type = actualSpell.getTypeAndTraits();

            copy.set(mouseX - width * 0.5F - x * 0.5F, mouseY - height * 0.5F - y * 0.5F, 0, 0);

            DrawableUtil.drawLine(context, 0, 0, (int)x, (int)y, 0xFF000000 | type.type().getColor());


            int color = type.type().getColor() << 2;

            matrices.pushMatrix();
            matrices.translate(x, y);

            matrices.pushMatrix();
            matrices.rotate((float)Math.toRadians(((Minecraft.getInstance().player.tickCount + tickDelta) * 2) % 360));
            DrawableUtil.renderItemIcon(context, actualSpell.isDead() ? UItems.BOTCHED_GEM.getDefaultInstance() : type.getDefaultStack(),
                    -8,
                    -8,
                    1
            );
            matrices.popMatrix();

            boolean hovered = isMouseOver(relativeMouseX, relativeMouseY);
            double radius = (hovered ? 9 + Mth.sin((Minecraft.getInstance().player.tickCount + tickDelta) / 9F) : 7);

            DrawableUtil.drawArc(context, radius, radius + 1, 0, DrawableUtil.TAU, color | 0x00000088);

            if (hovered) {
                DrawableUtil.drawArc(context, 0, 8, 0, DrawableUtil.TAU, color | 0x000000FF);

                List<Component> tooltip = new ArrayList<>();

                MutableComponent name = type.type().getName().copy();
                color = type.type().getColor();
                name.setStyle(name.getStyle().withColor(color == 0 ? 0xFFAAAAAA : color));
                tooltip.add(Component.translatable("gui.unicopia.dispell_screen.spell_type", name));
                type.traits().appendTooltip(tooltip::add);
                tooltip.add(CommonComponents.EMPTY);
                type.addToTooltip(TooltipContext.of(minecraft.level), tooltip::add, TooltipFlag.NORMAL, DataComponentMap.EMPTY);
                tooltip.add(CommonComponents.EMPTY);
                if (spell instanceof TimedSpell timed) {
                    tooltip.add(CommonComponents.EMPTY);
                    float tickRate = Minecraft.getInstance().level.tickRateManager().tickrate();
                    tooltip.add(Component.translatable("gui.unicopia.dispell_screen.time_left", StringUtil.formatTickDuration(timed.getTimer().getTicksRemaining(), tickRate)));
                }
                tooltip.add(CommonComponents.EMPTY);
                tooltip.add(Component.translatable("gui.unicopia.dispell_screen.discard"));
                context.setComponentTooltipForNextFrame(getFont(), tooltip, mouseX, mouseY);

                if (!lastMouseOver) {
                    lastMouseOver = true;
                    playClickEffect();
                }
            } else {
                lastMouseOver = false;
            }

            matrices.popMatrix();
        }

        @Override
        public void updateNarration(NarrationElementOutput builder) {
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.HOVERED;
        }

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return false;
        }
    }

    static void playClickEffect() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(USounds.Vanilla.UI_BUTTON_CLICK.value(), 6, 0.3F));
    }

    static double squareDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return (dx * dx + dy * dy);
    }
}
