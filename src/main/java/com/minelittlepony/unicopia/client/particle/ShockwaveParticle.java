package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ShockwaveParticle extends AbstractBillboardParticle {
    private static final Identifier TEXTURE = Unicopia.id("textures/particles/shockwave.png");

    public ShockwaveParticle(SimpleParticleType effect, ClientLevel world, double x, double y, double z, double velocityX, double velocityY,
            double velocityZ) {
        super(world, x, y, z, 0, 0, 0);
        lifetime = 20;
        setParticleSpeed(0, 0, 0);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderQuads(VertexConsumer buffer, float x, float y, float z, float tickDelta) {
        if (age < 5 || age % 6 == 0) {
            BlockState state = level.getBlockState(BlockPos.containing(this.x, this.y - 0.5, this.z));
            if (!state.isAir()) {
                level.playLocalSound(this.x, this.y, this.z, state.getSoundType().getBreakSound(), SoundSource.AMBIENT, 2.5F, 0.4F, true);
            }
        }

        PoseStack matrices = new PoseStack();
        matrices.translate(x, y, z);

        for (int ring = -1; ring < 2; ring ++) {
            float scaleH = (1.5F + (age + tickDelta)) + ring;
            float scaleV = (2 + Mth.sin(age / 5F) * 2F) - Math.abs(ring);
            matrices.pushPose();
            matrices.scale(scaleH, scaleV, scaleH);
            matrices.translate(-0.5, 0, -0.5);
            int sides = 5;
            for (int i = 0; i < sides; i++) {
                RenderUtil.renderFace(matrices, buffer, rCol, gCol, bCol, 0.3F, LightCoordsUtil.FULL_BRIGHT);
                matrices.rotate(Axis.YP.rotationDegrees(360 / sides));
                matrices.translate(-1, 0, 0);
            }
            matrices.popPose();
        }
    }
}
