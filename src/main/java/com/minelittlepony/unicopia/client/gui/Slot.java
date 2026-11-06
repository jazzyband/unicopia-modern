package com.minelittlepony.unicopia.client.gui;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.ability.ActivationType;
import com.minelittlepony.unicopia.client.KeyBindingsHandler;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import org.joml.Matrix3x2fStack;

class Slot {
    protected final UHud uHud;

    private final AbilitySlot aSlot;
    private final AbilitySlot bSlot;

    protected int x;
    protected int y;

    private float lastCooldown;

    private final int slotPadding;
    private final int labelX;
    private final int labelY;

    protected final int size;
    private final int iconSize;

    private int backgroundU;
    private int backgroundV;

    private int foregroundU = 105;
    private int foregroundV = 105;

    public Slot(UHud uHud, AbilitySlot normalSlot, AbilitySlot backupSlot, int x, int y) {
        this(uHud, normalSlot, backupSlot, x, y, 3, 22, 17, 17, 19);
        background(80, 105);
    }

    public Slot(UHud uHud, AbilitySlot normalSlot, AbilitySlot backupSlot, int x, int y, int padding, int size, int labelX, int labelY, int iconSize) {
        this.uHud = uHud;
        this.aSlot = normalSlot;
        this.bSlot = backupSlot;
        this.x = x;
        this.y = y;
        this.slotPadding = padding;
        this.labelX = labelX;
        this.labelY = labelY;
        this.size = size;
        this.iconSize = iconSize;
    }

    Slot background(int u, int v) {
        backgroundU = u;
        backgroundV = v;
        return this;
    }
    Slot foreground(int u, int v) {
        foregroundU = u;
        foregroundV = v;
        return this;
    }

    int getX() {
        if (uHud.xDirection < 0) {
            return UHud.PRIMARY_SLOT_SIZE - size - x;
        }

        return x;
    }

    int getY() {
        return y;
    }

    void renderBackground(GuiGraphicsExtractor context, AbilityDispatcher abilities, boolean bSwap, float tickDelta) {

        if (aSlot != bSlot) {
            bSwap |= !abilities.isFilled(aSlot);
            bSwap &= abilities.isFilled(bSlot);
        }

        int page = Unicopia.getConfig().hudPage.get();
        AbilityDispatcher.Stat stat = abilities.getStat(bSwap ? bSlot : aSlot);

        if (stat.getAbility(page).isEmpty()) {
            if (aSlot != AbilitySlot.PRIMARY
                    || (!abilities.getStat(AbilitySlot.SECONDARY).getAbility(page).isEmpty()
                    && !abilities.getStat(AbilitySlot.TERTIARY).getAbility(page).isEmpty())) {
                return;
            }
        }

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(getX(), getY());

        // background
        context.blit(RenderPipelines.GUI_TEXTURED, UHud.HUD_TEXTURE, 0, 0, backgroundU, backgroundV, size, size, 128, 128);

        int iconPosition = ((size - iconSize + slotPadding + 1) / 2);
        int sz = iconSize - slotPadding;
        uHud.renderAbilityIcon(context, stat, iconPosition, iconPosition, sz, sz, sz, sz);

        float cooldown = stat.getFillProgress();

        if (cooldown > 0 && cooldown <= 1) {
            float lerpCooldown = Mth.lerp(tickDelta, cooldown, lastCooldown);

            lastCooldown = lerpCooldown;

            int progressBottom = size - slotPadding;
            int progressMax = size - slotPadding * 2;
            int progressTop = progressBottom - (int)(progressMax * cooldown);

            // progress
            context.fill(slotPadding, progressTop, size - slotPadding, progressBottom, 0xCFFFFFFF);
        }

        renderContents(context, abilities, bSwap, tickDelta);
        matrices.popMatrix();
    }

    protected void renderContents(GuiGraphicsExtractor context, AbilityDispatcher abilities, boolean bSwap, float tickDelta) {
        renderForegroundLayer(context, 0, 0);
    }

    protected void renderForegroundLayer(GuiGraphicsExtractor context, int uOffset, int vOffset) {
        // contents
        boolean flip = Minecraft.getInstance().player.getMainArm() == HumanoidArm.LEFT;
        if (flip) {
            context.blit(RenderPipelines.GUI_TEXTURED, UHud.HUD_TEXTURE, 0, 0, size, size, foregroundU + uOffset + size, foregroundV + vOffset, -size, size, 128, 128);
        } else {
            context.blit(RenderPipelines.GUI_TEXTURED, UHud.HUD_TEXTURE, 0, 0, foregroundU + uOffset, foregroundV + vOffset, size, size, 128, 128);
        }
    }

    void renderLabel(GuiGraphicsExtractor context, AbilityDispatcher abilities, float tickDelta) {

        Optional<Ability<?>> ability = abilities.getStat(aSlot).getAbility(Unicopia.getConfig().hudPage.get());

        if (ability.isEmpty()) {
            return;
        }

        MutableComponent label = KeyBindingsHandler.INSTANCE.getBinding(aSlot).getLabel().copy();

        int x = getX();
        if (uHud.xDirection > 0) {
            x += labelX;
        } else {
            x += labelX - size/3;
            x -= uHud.client.font.width(label)/2;
        }

        ActivationType activation = KeyBindingsHandler.INSTANCE.getForcedActivationType();
        if (activation.isResult()) {
            label = label.append("+T" + activation.getTapCount());
            if (!ability.get().acceptsQuickAction(UnicopiaClient.getClientPony(), activation)) {
                label = label.withStyle(ChatFormatting.RED);
            }
        }

        DrawableUtil.drawScaledText(context, label, x, getY() + labelY, 0.5F, CommonColors.WHITE);
    }
}
