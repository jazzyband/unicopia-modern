package com.minelittlepony.unicopia.client.particle;

import org.joml.Vector4f;

import com.minelittlepony.unicopia.client.render.model.FanModel;
import com.minelittlepony.unicopia.client.render.model.VertexLightSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class DustCloudParticle extends AbstractBillboardParticle {
    protected static final int SEGMENTS = 20;
    protected static final int SEPARATION = 270 / SEGMENTS;

    private float scaleFactor;

    protected TextureAtlasSprite sprite;
    private final FanModel model;

    private final VertexLightSource lightSource;

    public DustCloudParticle(BlockParticleOption effect, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        lifetime = 1000;
        gravity = 1;
        rCol = 0.6F;
        gCol = 0.6F;
        bCol = 0.6F;
        alpha = (float)world.getRandom().triangle(0.6, 0.2) * 0.3F;
        scaleFactor = (float)world.getRandom().triangle(2, 1.2);
        sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(effect.getState()).sprite();
        lightSource = new VertexLightSource(world);
        model = new FanModel(sprite) {
            @Override
            protected int getLightAt(Vector4f pos, int light) {
                return lightSource.getLight(pos, light);
            }
        };
        if (!effect.getState().is(Blocks.GRASS_BLOCK)) {
            var tint = Minecraft.getInstance().getBlockColors().getTintSource(effect.getState(), 0);
            int i = tint == null ? CommonColors.WHITE : tint.colorInWorld(effect.getState(), world, BlockPos.containing(x, y, z));
            rCol *= ARGB.redFloat(i);
            gCol *= ARGB.greenFloat(i);
            bCol *= ARGB.blueFloat(i);
        }
    }

    @Override
    protected Identifier getTexture() {
        return sprite.atlasLocation();
    }

    @Override
    public void tick() {
        super.tick();
        scaleFactor += 0.001F;
        scale(Mth.clamp(age / 5F, 0, 1) * scaleFactor);
        lightSource.tick();
    }

    @Override
    protected void renderQuads(VertexConsumer buffer, float x, float y, float z, float tickDelta) {
        float scale = getScale(tickDelta) * 0.5F;
        float alpha = this.alpha * (1 - ((float)age / lifetime));
        int color = ARGB.color((int)(alpha * 255), CommonColors.WHITE);
        PoseStack matrices = new PoseStack();
        matrices.translate(x, y, z);
        matrices.scale(1, 0.5F, 1);

        float angle = (Mth.sin((this.age + tickDelta) / 100F) * 360) / SEGMENTS;

        for (int i = 0; i < SEGMENTS; i++) {
            matrices.pushPose();
            matrices.rotate(Axis.XP.rotationDegrees((i * angle)));
            matrices.rotate(Axis.YP.rotationDegrees((SEPARATION * i - angle)));
            matrices.rotate(Axis.ZP.rotationDegrees((SEPARATION * i + angle)));
            float ringScale = 1 + Mth.sin(((i * 10) + age + tickDelta) * 0.05F) * 0.1F;

            model.render(matrices, buffer, 0, scale * ringScale, color);
            matrices.popPose();
        }
    }
}
