package com.minelittlepony.unicopia.client.render.spell;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public abstract class SpellRenderer<T extends Spell, S extends SpellRenderer.SpellRenderState> {
    public static final SpellRenderer<?, ?> DEFAULT = new SpellRenderer<>() {
        @Override
        public SpellRenderState createRenderState() {
            return new SpellRenderState();
        }
    };

    protected final Minecraft client = Minecraft.getInstance();

    private final S state = createRenderState();

    public final S getAndUpdateRenderState(T spell, Caster<?> caster, float tickDelta) {
        updateRenderState(spell, state, caster, tickDelta);
        return state;
    }

    public void updateRenderState(T spell, S state, Caster<?> caster, float tickDelta) {
        state.uuid = spell.getUuid();
        state.type = spell.getTypeAndTraits();
        client.getItemModelResolver().updateForNonLiving(state.gemstone, state.type.getDefaultStack(), ItemDisplayContext.FIXED, caster.asEntity());
    }

    public abstract S createRenderState();

    public boolean shouldRenderEffectPass(int pass) {
        return true;
    }

    public void render(PoseStack matrices, SubmitNodeCollector vertices, S spell, CasterState caster, int light) {
        if (caster.isCamera || caster.isProjectile) {
            return;
        }

        if (EquinePredicates.IS_CASTER.test(client.player)) {
            renderGemstone(matrices, vertices, spell, caster, light);
        }
    }

    private void renderGemstone(PoseStack matrices, SubmitNodeCollector vertices, S spell, CasterState caster, int light) {
        matrices.pushPose();
        float scale = 1/8F;
        matrices.scale(scale, scale, scale);

        transformGemstone(matrices, vertices, spell, caster);
        matrices.pushPose();
        matrices.rotate(Axis.YP.rotationDegrees(caster.entityState.ageInTicks));

        spell.gemstone.submit(matrices, vertices, light, 0, CommonColors.WHITE);
        matrices.popPose();

        if (spell instanceof TimedSpell timed) {
            if (caster.gemYaw != 0) {
                matrices.rotate(Axis.YP.rotationDegrees(caster.gemYaw));
            }
            renderCountdown(matrices, vertices, timed, caster.entityState.ageInTicks);
        }

        matrices.popPose();
    }

    protected void renderCountdown(PoseStack matrices, SubmitNodeCollector vertices, TimedSpell spell, float tickDelta) {
        matrices.rotate(client.gameRenderer.mainCamera().rotation().invert(new Quaternionf()));
        float radius = 0.6F;
        float timeRemaining = spell.getTimer().getPercentTimeRemaining(tickDelta);

        SpellGeometry.render(matrices, vertices, net.minecraft.client.renderer.rendertype.RenderTypes.lines(), (stack, buffer) -> {
            int segments = Math.max(3, (int)(timeRemaining * 64));
            int color = ARGB.srgbLerp(Mth.clamp(timeRemaining * 4, 0, 1), CommonColors.BLUE, CommonColors.WHITE);
            for (int i = 0; i < segments; i++) {
                float a = (float)(DrawableUtil.TAU * timeRemaining * i / segments);
                float b = (float)(DrawableUtil.TAU * timeRemaining * (i + 1) / segments);
                float x1 = Mth.cos(a) * radius, y1 = Mth.sin(a) * radius;
                float x2 = Mth.cos(b) * radius, y2 = Mth.sin(b) * radius;
                buffer.addVertex(stack.last().pose(), x1, y1, 0).setColor(color).setNormal(stack.last(), 0, 0, 1);
                buffer.addVertex(stack.last().pose(), x2, y2, 0).setColor(color).setNormal(stack.last(), 0, 0, 1);
            }
        });
    }

    protected void transformGemstone(PoseStack matrices, SubmitNodeCollector vertices, S spell, CasterState caster) {
        float y = caster.isPlacement ? 1 : -caster.entityState.boundingBoxHeight;
        matrices.translate(0, y * 8 + Mth.sin(caster.entityState.ageInTicks / 3F) * 0.2F, 0);
    }

    public static class SpellRenderState {
        public UUID uuid;
        public CustomisedSpellType<?> type;
        public final ItemStackRenderState gemstone = new ItemStackRenderState();
    }
}
