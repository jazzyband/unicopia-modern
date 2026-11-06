package com.minelittlepony.unicopia.client.gui;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgPlayerTargetEntity;
import com.minelittlepony.unicopia.util.Trace;
import com.mojang.blaze3d.vertex.PoseStack;

public class LevitatingItemActionWheel {

    @Nullable
    public LevitatingItemEntity targetEntity;
    private Vec3 targetPosition = Vec3.ZERO;

    @Nullable
    public LevitatingItemEntity.Action currentAction;

    private int unfocusDelay;

    private final Minecraft client = Minecraft.getInstance();

    public void tick(Minecraft client, Pony pony) {
        if (currentAction == LevitatingItemEntity.Action.MOVE && targetEntity != null && unfocusDelay > 0) {
            unfocusDelay--;
            pony.setLookedEntity(targetEntity);
            return;
        }
        if (client.crosshairPickEntity instanceof LevitatingItemEntity target) {
            targetEntity = target;
        } else if (pony.asEntity().getRootVehicle() instanceof LevitatingItemEntity target) {
            targetEntity = target;
        } else {
            targetEntity = Trace.create(pony.asEntity(), 100, client.getDeltaTracker().getGameTimeDeltaPartialTick(false), e -> {
                return e instanceof LevitatingItemEntity;
            }).<LevitatingItemEntity>getEntity().orElse(null);
        }
        if (targetEntity != null) {
            targetPosition = targetEntity.position();
        }
        pony.setLookedEntity(targetEntity);
    }


    public boolean handleInput(ClientInput input) {
        if (currentAction == LevitatingItemEntity.Action.MOVE && targetEntity != null) {
            Vec2 horizontalMovement = input.getMoveVector();
            int verticalMovement = input.keyPresses.jump() ? 1 : input.keyPresses.shift() ? -1 : 0;

            if (horizontalMovement.length() > 1.0E-5F || verticalMovement != 0) {
                unfocusDelay = 30;
                input.keyPresses = Input.EMPTY;

                Channel.CLIENT_PLAYER_LOOK_AT_ENTITY.sendToServer(new MsgPlayerTargetEntity(
                        Optional.of(targetEntity.getId()),
                        Optional.of(UHud.INSTANCE.levitatingItemActions.currentAction),
                        Optional.of(new Vec3(horizontalMovement.x, verticalMovement, horizontalMovement.y)
                                .yRot(-client.gameRenderer.mainCamera().yaw() * Mth.DEG_TO_RAD))
                ));
            }
        }
        return currentAction == LevitatingItemEntity.Action.MOVE;
    }

    private Vector2i getCrosshairPos() {
        var client = Minecraft.getInstance();
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();

        Camera cam = client.gameRenderer.mainCamera();
        Vec3 offset = (currentAction == LevitatingItemEntity.Action.MOVE ? targetPosition : targetEntity.position()).add(0, targetEntity.getBbHeight() * 0.5F, 0).subtract(cam.position());
        Quaternionf rotation = cam.rotation().conjugate(new Quaternionf());
        Matrix4f matrix = new Matrix4f().identity().rotate(rotation);
        Vector4f projectedPosition = matrix.transform(new Vector4f((float)offset.x, (float)offset.y, (float)offset.z, 0));

        int crosshairX = -(int)(projectedPosition.x * scaledWidth);
        int crosshairY =  (int)(projectedPosition.y * scaledHeight);

        return new Vector2i(crosshairX, crosshairY);
    }

    public void render(GuiGraphicsExtractor context, float tickDelta) {
        if (targetEntity == null) {
            currentAction = null;
            return;
        }
        Pony.of(client.player).setLookedEntity(targetEntity);

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();


        Vector2i crosshair = getCrosshairPos();

        Matrix3x2fStack matrices = context.pose();

        matrices.pushMatrix();
        matrices.translate(scaledWidth * 0.5F, scaledHeight * 0.5F);
        DrawableUtil.drawLine(context, crosshair.x, crosshair.y - 5, crosshair.x, crosshair.y + 5, CommonColors.WHITE);
        DrawableUtil.drawLine(context, crosshair.x - 5, crosshair.y, crosshair.x + 5, crosshair.y, CommonColors.WHITE);

        drawOptionsWheel(context, crosshair, targetEntity.getDefaultAction(), targetEntity.getValidActions());

        matrices.popMatrix();
    }

    private void drawOptionsWheel(GuiGraphicsExtractor context, Vector2i crosshair, LevitatingItemEntity.Action defaultAction, List<LevitatingItemEntity.Action> options) {
        int ringInnerDiameter = 30;
        int ringOuterDiameter = 55;
        int ringMarkerWidth = 3;

        int iconSize = 17;
        int segmenticonRadius = ringInnerDiameter + ringInnerDiameter / 2;
        int segmentCount = options.size();
        double segmentAngle = DrawableUtil.TAU / segmentCount;
        double segmentsStartAngle = DrawableUtil.PI - segmentAngle / 2;

        double theta = Math.atan2(crosshair.x, -crosshair.y) + Math.PI;
        double rad = Math.max(crosshair.y / Math.cos(theta), -crosshair.x / Math.sin(theta));

        int selection = getSelection(theta, rad, segmentsStartAngle, segmentAngle);

        currentAction = selection < 0 || selection >= options.size() ? defaultAction : options.get(selection);

        DrawableUtil.drawArc(context, ringInnerDiameter, ringInnerDiameter + 1, 0, DrawableUtil.TAU, 0xFFFFFF22);
        DrawableUtil.drawArc(context, ringInnerDiameter, ringOuterDiameter, 0, DrawableUtil.TAU, 0x00000055);
        DrawableUtil.drawArc(context, ringOuterDiameter, ringOuterDiameter + ringMarkerWidth, theta - segmentAngle / 2, segmentAngle, 0xFFFFFF45);

        Component selectedItemLabel = currentAction.getLabel();
        context.text(client.font, selectedItemLabel, -client.font.width(selectedItemLabel) / 2, -client.font.lineHeight / 2, CommonColors.WHITE, true);

        for (int i = 0; i < segmentCount; i++) {
            double segmentMinAngle = (i * segmentAngle + segmentsStartAngle) % DrawableUtil.TAU;

            if (i == selection) {
                DrawableUtil.drawArc(context, ringInnerDiameter, ringOuterDiameter, segmentMinAngle, segmentAngle, 0xFFFFFF25);
            }

            LevitatingItemEntity.Action action = options.get(i);
            float iconPositionAngle = (float)(-segmentMinAngle - segmentAngle * 0.5F);
            int x = (int)(Mth.sin(iconPositionAngle) * segmenticonRadius);
            int y = (int)(Mth.cos(iconPositionAngle) * segmenticonRadius);

            context.blit(RenderPipelines.GUI_TEXTURED, UHud.HUD_TEXTURE, x - iconSize / 2, y - iconSize / 2, 144 + action.getU() * iconSize, action.getV() * iconSize, iconSize, iconSize, 256, 256);
        }
    }

    private int getSelection(double theta, double rad, double firstSegmentStart, double segmentSize) {
        if (rad <= 0) {
            return -1;
        }

        theta -= firstSegmentStart;
        if (theta < 0) {
            theta += DrawableUtil.TAU;
        }
        theta %= DrawableUtil.TAU;
        theta /= segmentSize;


        return (int)theta;
    }
}
