package com.minelittlepony.unicopia.client.gui;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.*;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.spell.DarkVortexSpellRenderer;
import com.minelittlepony.unicopia.client.sound.*;
import com.minelittlepony.unicopia.entity.ItemTracker;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;
import com.minelittlepony.unicopia.entity.effect.SunBlindnessStatusEffect;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.GlassesItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.TypedActionResult;
import org.joml.Matrix3x2fStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stat;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;


import net.minecraft.world.item.Items;

public class UHud {

    public static final UHud INSTANCE = new UHud();

    public static final Identifier HUD_TEXTURE = Unicopia.id("textures/gui/hud.png");
    public static final int HUD_TEXTURE_SIZE = 128;

    public static final int PRIMARY_SLOT_SIZE = 49;

    private static final float EQUIPPED_GEMSTONE_SCALE = 0.7F;

    public Font font;

    final Minecraft client = Minecraft.getInstance();

    private final List<Slot> slots = List.of(
        new ManaRingSlot(this, AbilitySlot.PRIMARY, AbilitySlot.PASSIVE, 0, 0),
        new Slot(this, AbilitySlot.SECONDARY, AbilitySlot.SECONDARY, 30, -10),
        new Slot(this, AbilitySlot.TERTIARY, AbilitySlot.TERTIARY, 43, 10)
    );
    public final LevitatingItemActionWheel levitatingItemActions = new LevitatingItemActionWheel();

    @Nullable
    private Component message;
    private int messageTime;

    int xDirection;

    @Nullable
    private LoopingSoundInstance<Player> heartbeatSound;
    @Nullable
    private LoopingSoundInstance<Player> partySound;

    private boolean prevPointed;
    private boolean prevReplacing;
    private SpellType<?> focusedType = SpellType.empty();

    public void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        if (client.player == null) {
            return;
        }

        int scaledWidth = context.guiWidth();
        int scaledHeight = context.guiHeight();
        Matrix3x2fStack matrices = context.pose();

        Pony pony = Pony.of(client.player);

        float tickDelta = tickCounter.getGameTimeDeltaPartialTick(false);

        matrices.pushMatrix();
        renderViewEffects(pony, context, tickDelta);
        matrices.popMatrix();

        if (client.gui.screen() instanceof HidesHud || client.player.isSpectator() || client.gui.hud.isHidden()) {
            return;
        }

        font = client.font;

        HudPosition hudPos = Unicopia.getConfig().hudPosition.get();
        HudPosition.Alignment armAlignment = client.player.getMainArm() == HumanoidArm.LEFT ? HudPosition.Alignment.START : HudPosition.Alignment.END;
        if (hudPos == HudPosition.OFF_HAND) {
            armAlignment = armAlignment.opposite();
        }

        xDirection = hudPos.getHorizontal().or(armAlignment.opposite()).opposite().getSignum();

        levitatingItemActions.render(context, tickDelta);

        matrices.pushMatrix();
        matrices.translate(scaledWidth / 2, scaledHeight / 2);

        float flapCooldown = pony.getPhysics().getFlapCooldown(tickDelta);
        if (flapCooldown > 0) {
            float angle = Mth.TWO_PI * flapCooldown;
            DrawableUtil.drawArc(context, 3, 6, -angle / 2F, angle, 0x888888AF);
        }

        matrices.popMatrix();
        matrices.pushMatrix();

        int hudX = hudPos.getHorizontal().pick(2, scaledWidth - 50, ((scaledWidth - 50) / 2) + (109 * armAlignment.getSignum()));
        int hudY = hudPos.getVertical().pick(12, scaledHeight - 50, scaledHeight - 50);
        if (hudPos == HudPosition.BOTTOM_CENTER) {
            hudY -= 22;
        }

        float exhaustion = pony.getMagicalReserves().getExhaustion().getPercentFill();

        if (exhaustion > 0.5F || EquinePredicates.RAGING.test(client.player)) {
            RandomSource rng = client.level.getRandom();
            hudX += rng.nextFloat() - 0.5F;
            hudY += rng.nextFloat() - 0.5F;
        }

        matrices.translate(hudX, hudY);

        AbilityDispatcher abilities = pony.getAbilities();

        if (message != null && messageTime > 0) {
            renderMessage(context, tickDelta);
        }

        boolean swap = client.options.keyShift.isDown();

        slots.forEach(slot -> slot.renderBackground(context, abilities, swap, tickDelta));

        int currentPage = Unicopia.getConfig().hudPage.get();
        int maxPages = pony.getAbilities().getMaxPage();

        Ability<?> ability = pony.getAbilities().getStat(AbilitySlot.PRIMARY)
                .getAbility(currentPage)
                .orElse(null);
        boolean canCast = ability == Abilities.CAST || ability == Abilities.KIRIN_CAST || ability == Abilities.SHOOT;

        if (canCast) {
            matrices.pushMatrix();
            matrices.translate(PRIMARY_SLOT_SIZE / 2F, PRIMARY_SLOT_SIZE / 2F);
            boolean first = !pony.asEntity().isShiftKeyDown();
            TypedActionResult<CustomisedSpellType<?>> inHand = pony.getCharms().getSpellInHand(false);
            boolean replacing = inHand.result().consumesAction() && pony.getAbilities().getStat(AbilitySlot.PRIMARY).getActiveAbility().isEmpty();
            if (first != prevPointed || replacing != prevReplacing || inHand.value().type() != focusedType) {
                focusedType = inHand.value().type();
                prevPointed = first;
                prevReplacing = replacing;
                setMessage(ability.getName(pony));
            }
            int baseAngle = xDirection < 0 ? 100 : 0;
            int secondAngleDif = xDirection * 30;
            matrices.rotate((baseAngle + 37 + (first ? 0 : secondAngleDif)) * Mth.DEG_TO_RAD);
            matrices.translate(-23, 0);
            matrices.rotate(-26 * Mth.DEG_TO_RAD);
            matrices.scale(0.8F, 0.8F);
            int u = replacing ? 16 : 3;
            context.blit(RenderPipelines.GUI_TEXTURED, HUD_TEXTURE, 0, 0, u, 120, 13, 7, HUD_TEXTURE_SIZE, HUD_TEXTURE_SIZE);
            matrices.popMatrix();
        }

        slots.forEach(slot -> slot.renderLabel(context, abilities, tickDelta));

        if (xDirection < 0) {
            matrices.translate(-48, 0);
        }

        //if (maxPages > 0) {
            DrawableUtil.drawScaledText(context, Component.literal((currentPage + 1) + "/" + (maxPages + 1)), 44, 38, 0.5F, CommonColors.WHITE);
            //down
            context.blit(RenderPipelines.GUI_TEXTURED, HUD_TEXTURE, 42, 43, 52, currentPage == 0 ? 6 : 0, 6, 6, HUD_TEXTURE_SIZE, HUD_TEXTURE_SIZE);
            //up
            context.blit(RenderPipelines.GUI_TEXTURED, HUD_TEXTURE, 48, 43, 57, currentPage < maxPages ? 0 : 6, 8, 6, HUD_TEXTURE_SIZE, HUD_TEXTURE_SIZE);
        //}

        matrices.popMatrix();

        if (canCast) {
            matrices.pushMatrix();
            if (xDirection < 0) {
                hudX += PRIMARY_SLOT_SIZE / 2F - 8;
            }
            SpellIconRenderer.renderSpell(context, pony.getCharms().getEquippedSpell(InteractionHand.MAIN_HAND), hudX + 10 - xDirection * 13, hudY + 2, EQUIPPED_GEMSTONE_SCALE);
            SpellIconRenderer.renderSpell(context, pony.getCharms().getEquippedSpell(InteractionHand.OFF_HAND), hudX + 8 - xDirection * 2, hudY - 6, EQUIPPED_GEMSTONE_SCALE);
            matrices.popMatrix();
        }
    }

    private void renderMessage(GuiGraphicsExtractor context, float tickDelta) {
        float time = messageTime - tickDelta;
        int progress = Math.min(255, (int)(time * 255F / 20F));

        if (progress > 8) {
            int color = ARGB.color(progress, CommonColors.WHITE);

            HudPosition hudPos = Unicopia.getConfig().hudPosition.get();

            int messageWidth = font.width(message);
            int messageX = hudPos.getHorizontal().pick(0, 25 - messageWidth/2, -messageWidth + 45, 25 - messageWidth/2);
            int messageY = hudPos.getVertical().pick(55, -17, -17, -17);
            context.text(font, message, messageX, messageY, color, true);
        }
    }

    protected void renderViewEffects(Pony pony, GuiGraphicsExtractor context, float tickDelta) {

        float vortexDistortion = DarkVortexSpellRenderer.getCameraDistortion();

        int scaledWidth = context.guiWidth();
        int scaledHeight = context.guiHeight();

        if (vortexDistortion > 25) {
            context.fill(0, 0, scaledWidth, scaledHeight, 0xDD000022);
            context.pose().pushMatrix();
            context.pose().translate(scaledWidth / 2, scaledHeight / 2);
            DrawableUtil.drawArc(context, 0, 20, 0, Mth.TWO_PI, 0x000000FF);
            context.pose().popMatrix();
            return;
        } else if (vortexDistortion > 0) {
            context.fill(0, 0, scaledWidth, scaledHeight, (int)((Math.min(20, vortexDistortion) / 20F) * 255) << 24);
        }

        boolean hasEffect = client.player.hasEffect(UEffects.SUN_BLINDNESS);

        ItemStack glasses = GlassesItem.getForEntity(client.player).stack();

        if (hasEffect || (!glasses.is(UTags.Items.PROTECTS_BAT_PONY_EYES) && pony.getObservedSpecies() == Race.BAT && SunBlindnessStatusEffect.hasSunExposure(client.player))) {
            float i = hasEffect ? (client.player.getEffect(UEffects.SUN_BLINDNESS).getDuration() - tickDelta) / SunBlindnessStatusEffect.MAX_DURATION : 0;

            float pulse = (1 + (float)Math.sin(client.player.tickCount / 108F)) * 0.25F;

            float strength = Mth.clamp(pulse + i, 0.3F, 1F);

            int alpha1 = (int)(strength * 205);
            int alpha2 = (int)(alpha1 * 0.6F);
            int color = 0xFFFFFF;

            if (hasEffect) {
                GradientUtil.fillRadialGradient(context, 0, 0, scaledWidth, scaledHeight,
                        color | (alpha1 << 24),
                        color | (alpha2 << 24),
                        0, 1);
            } else {
                GradientUtil.fillVerticalGradient(context, 0, 0, scaledHeight / 2, scaledWidth, scaledHeight,
                        color | (alpha1 << 24),
                        color | (alpha2 << 24),
                        color | (alpha1 << 24),
                        0);
            }
        }

        if (glasses.is(UTags.Items.TINTED_SHADES)) {
            if (GlassesItem.isCoolAndHasShades(client.player)) {
                final int delay = 7;
                final int current = client.player.tickCount / delay;
                final int tint = DyeColor.byId(current % DyeColor.values().length).getTextColor();
                context.fillGradient(0, 0, scaledWidth, scaledHeight, 0x1F000000 | tint, 0x5F000000 | tint);

                if (partySound == null || partySound.isStopped()) {
                    client.getSoundManager().play(
                            partySound = new LoopingSoundInstance<>(client.player, GlassesItem::isCoolAndHasShades, USounds.Vanilla.MUSIC_DISC_PIGSTEP.value(), 1, 1, client.level.getRandom())
                    );
                } else if (partySound != null) {
                    partySound.setMuted(false);
                }
            } else {
                if (partySound != null) {
                    partySound.setMuted(true);
                }
                context.fillGradient(0, 0, scaledWidth, scaledHeight, 0x0A000088, 0x7E000000);
            }
        } else {
            if (partySound != null) {
                partySound.setMuted(true);
            }
        }

        if (AmuletItem.isApplicable(client.player, UItems.ALICORN_AMULET)) {
            float radius = (float)pony.getArmour().getTicks(UItems.ALICORN_AMULET) / (5 * ItemTracker.DAYS);
            renderVignette(context, 0x000000, radius, radius, scaledWidth, scaledHeight);
        }

        float exhaustion = Mth.clamp(pony.getMagicalReserves().getExhaustion().getPercentFill(), 0, 0.6F);

        if (exhaustion > 0) {
            if (exhaustion > 0.5F && (heartbeatSound == null || heartbeatSound.isStopped())) {
                client.getSoundManager().play(
                        heartbeatSound = new LoopingSoundInstance<>(client.player, player -> {
                            return partySound == null && Pony.of(player).getMagicalReserves().getExhaustion().getPercentFill() > 0.5F;
                        }, USounds.ENTITY_PLAYER_HEARTBEAT_LOOP, 1, 1, client.level.getRandom())
                );
            }

            float rate = exhaustion > 0.5F ? 2.5F : 7F;
            float radius = (1 + (float)Math.sin(client.player.tickCount / rate)) / 2F;

            renderVignette(context, 0x880000, exhaustion * radius, 0.1F + radius * 0.3F, scaledWidth, scaledHeight);
        }

        float anger = pony.getMagicalReserves().getCharge().getPercentFill();

        if (pony.getObservedSpecies() == Race.KIRIN && anger >= 1F) {
            float radius = (1 + (float)Math.sin(client.player.tickCount / 25F)) / 5F;
            renderVignette(context, 0x000000, anger * radius, 0.1F + radius * 0.3F, scaledWidth, scaledHeight);
        }

        if (EquinePredicates.RAGING.test(client.player)) {
            context.fillGradient(0, 0, scaledWidth, scaledHeight / 4, 0xAAFF0000, 0x00FF0000);
            int alpha = 0x3A + (int)(125 * Math.abs(Mth.sin(client.player.tickCount / 25F)));//3A
            context.fill(0, 0, scaledWidth, scaledHeight, 0x00FF0000 | (alpha << 24));
            context.fillGradient(0, (int)(scaledHeight / 1.5), scaledWidth, scaledHeight, 0x00FF0000, 0xAAFF0000);
        }

        if (pony.getPhysics().isFlyingSurvival) {
            renderVelocityOverlay(pony, context, tickDelta);
        }
    }

    private void renderVelocityOverlay(Pony pony, GuiGraphicsExtractor context, float tickDelta) {
        float effectStrength = (float)Mth.clamp(pony.getPhysics().getTrackedVelocity().length() / 15F, 0, 1);

        int scaledWidth = context.guiWidth();
        int scaledHeight = context.guiHeight();

        float innerRadiusPulse = Mth.cos((pony.asEntity().tickCount + tickDelta) / 2F) * 6 + (effectStrength * scaledHeight / 2F);

        double points = 22;
        float wedgeAngle = 0.05F + Mth.sin((pony.asEntity().tickCount + tickDelta) / 3F) * 0.01F;
        float outerRadius = Math.max(scaledWidth, scaledHeight);
        float alpha = effectStrength * (0.6F + Math.abs(Mth.sin((pony.asEntity().tickCount + tickDelta) / 10F)));
        context.pose().pushMatrix();
        context.pose().translate(scaledWidth / 2F, scaledHeight / 2F);
        for (int i = 0; i < points; i++) {
            float angle = (Mth.TWO_PI * i / (float)points) - wedgeAngle * 0.5F;
            float innerRadius = Math.max(0, (scaledHeight / 2F) + (i % 2) * 72 + 14 * (1 - effectStrength) - innerRadiusPulse);
            DrawableUtil.drawArc(context, innerRadius, outerRadius, angle - wedgeAngle, wedgeAngle * 2,
                    ((int)(alpha * 120) << 24) | 0xFFFFFF);
        }
        context.pose().popMatrix();
    }

    private void renderVignette(GuiGraphicsExtractor context, int color, float alpha, float radius, int scaledWidth, int scaledHeight) {
        if (radius <= 0) {
            return;
        }

        color &= 0xFFFFFF;
        float alpha2 = Mth.clamp(radius - 1, 0, 1) * 255;
        float alpha1 = Math.max(alpha2, Mth.clamp(alpha * 2, 0, 1) * 205);
        GradientUtil.fillRadialGradient(context, 0, 0, scaledWidth, scaledHeight,
                color | (int)alpha1 << 24,
                color | (int)alpha2 << 24,
                0, Math.min(1, radius));
    }

    public void setMessage(Component message) {
        this.message = message;
        this.messageTime = 60;
    }

    public void tick() {
        if (!client.isPaused() && messageTime > 0) {
            messageTime--;
        }
        if (!client.isPaused() && client.player != null) {
            levitatingItemActions.tick(client, Pony.of(client.player));
        }
    }

    public boolean handleInput(Pony pony, ClientInput input) {
        return levitatingItemActions.handleInput(input) || pony.getAcrobatics().isParalized();
    }

    void renderAbilityIcon(GuiGraphicsExtractor context, AbilityDispatcher.Stat stat, int x, int y, int u, int v, int frameWidth, int frameHeight) {
        stat.getAbility(Unicopia.getConfig().hudPage.get()).ifPresent(ability -> {
            context.blit(RenderPipelines.GUI_TEXTURED, ability.getIcon(Pony.of(client.player)), x, y, 0, 0, frameWidth, frameHeight, u, v);
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object getHeartsType(Player player, Object vanillaHeartType) {
        Class heartType = ((Enum<?>)vanillaHeartType).getDeclaringClass();
        if (AmuletItem.isApplicable(player, UItems.ALICORN_AMULET) || EffectUtils.isChangingRace(player)) {
            return Enum.valueOf(heartType, "WITHERED");
        }

        if (EffectUtils.isPoisoned(player)) {
            return Enum.valueOf(heartType, "POISIONED");
        }

        return vanillaHeartType;
    }
}
