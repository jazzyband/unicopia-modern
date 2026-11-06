package com.minelittlepony.unicopia.client.particle;

import org.joml.Quaternionf;

import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.Vec3;

public class DiskParticle extends SphereParticle {

    private final Quaternionf rotation = new Quaternionf(0, 0, 0, 1);

    public DiskParticle(SphereParticleEffect effect, ClientLevel w, double x, double y, double z, double rX, double rY, double rZ) {
        super(effect.withOffset(new Vec3(0, 0.25, 0)), w, x, y, z, 0, 0, 0);

        rotation.mul(Axis.YP.rotationDegrees((float)effect.offset().y));
        rotation.mul(Axis.XP.rotationDegrees(180 - (float)effect.offset().x));
    }

    @Override
    protected void renderModel(PoseStack matrices, VertexConsumer buffer, float scale, float tickDelta, int light) {
        matrices.rotate(rotation);
        float thickness = 0.2F;
        SphereModel.DISK.render(matrices, buffer, light, 1, scale, CommonColors.WHITE);
        matrices.translate(0, -thickness, 0);
        SphereModel.DISK.render(matrices, buffer, light, 1, scale, CommonColors.WHITE);
    }
}

