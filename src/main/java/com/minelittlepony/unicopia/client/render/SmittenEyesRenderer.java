package com.minelittlepony.unicopia.client.render;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.ModelPartHooks.EnqueudHeadRender;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class SmittenEyesRenderer {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/smitten_eyes.png");

    private final Minecraft client = Minecraft.getInstance();
    private final ModelPart model;

    SmittenEyesRenderer() {
        MeshDefinition data = new MeshDefinition();
        data.getRoot().addOrReplaceChild("hearts", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4, -4, -4, 8.0f, 8.0f, 8.0f, CubeDeformation.NONE), PartPose.ZERO);
        model = LayerDefinition.create(data, 32, 32).bakeRoot();
    }

    public void render(Set<EnqueudHeadRender> headParts, Entity entity, PoseStack matrices, SubmitNodeCollector vertices, int light) {
        for (var part : headParts) {
            matrices.pushPose();
            part.transform(matrices, 0.95F);
            float scale = 1F + (1.3F + Mth.sin(entity.tickCount / 3F) * 0.06F);
            matrices.scale(scale, scale, scale);
            matrices.translate(0, 0.05F, 0);
            RenderGeometry.submit(matrices, vertices, RenderTypes.entityCutout(TEXTURE),
                    (stack, buffer) -> model.render(stack, buffer, light, OverlayTexture.NO_OVERLAY, CommonColors.WHITE));


            matrices.popPose();
        }
    }

    public boolean isSmitten(Creature pony) {
        return pony.isSmitten() || EnchantmentUtil.getWantItNeedItLevel(pony.asEntity()) > 0;
    }
}
