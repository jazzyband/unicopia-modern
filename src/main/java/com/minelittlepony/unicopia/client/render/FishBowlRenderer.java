package com.minelittlepony.unicopia.client.render;

import static net.minecraft.util.ARGB.colorFromFloat;

import java.util.Set;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.ModelPartHooks.EnqueudHeadRender;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.vertex.PoseStack;

public class FishBowlRenderer {
    private static final int DIVER_HELMET_COLOR = colorFromFloat(0.1F, 0.5F, 0.5F, 0.5F);

    public boolean shouldRender(Equine<?> equine) {
        return equine instanceof Pony pony && pony.getCompositeRace().includes(Race.SEAPONY)
                && equine.asEntity().isUnderWater()
                && MineLPDelegate.getInstance().getPlayerPonyRace(pony.asEntity()) != Race.SEAPONY;
    }

    public void render(
            Set<EnqueudHeadRender> headParts,
            PoseStack matrices,
            SubmitNodeCollector vertices,
            int light) {
        RenderType layer = RenderLayers.getMagicColored();
        for (var part : headParts) {
            matrices.pushPose();
            part.transform(matrices, 1F);
            float scale = 0.9F;

            vertices.submitCustomGeometry(matrices, layer, (pose, buffer) -> {
                PoseStack stack = new PoseStack();
                stack.mulPose(pose.pose());
                SphereModel.SPHERE.render(stack, buffer, light, 0, scale, DIVER_HELMET_COLOR);
                SphereModel.SPHERE.render(stack, buffer, light, 0, scale + 0.2F, DIVER_HELMET_COLOR);
            });

            matrices.popPose();
        }
    }
}
