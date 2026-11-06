package com.minelittlepony.unicopia.client.particle;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

public class LightningBoltParticle extends AbstractGeometryBasedParticle {

    private final List<Bolt> branches = new ArrayList<>();

    private final LightningBoltParticleEffect effect;

    public LightningBoltParticle(LightningBoltParticleEffect effect, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.effect = effect;
    }

    @Override
    public void tick() {
        if (age++ >= lifetime) {
            remove();
            return;
        }

        if (effect.changeFrequency() > 0 && age % effect.changeFrequency() == 0) {
            branches.clear();
        }

        if (branches.isEmpty()) {
            effect.pathEndPoint().ifPresentOrElse(endpoint -> {
                branches.add(generateTrunk(endpoint.subtract(x, y, z).toVector3f()));
            }, () -> {
                int totalBranches = 2 + level.getRandom().nextInt(effect.maxBranches());

                while (branches.size() < totalBranches) {
                    branches.add(generateBranch());
                }
            });

            if (!effect.silent()) {
                level.playLocalSound(x, y, z, USounds.Vanilla.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 10000, 8, true);
            }
        }

        if (effect.pathEndPoint().isEmpty() && !effect.silent()) {
            level.setSkyFlashTime(2);
        }
    }

    private Bolt generateTrunk(Vector3f end) {
        Vector3f start = new Vector3f(0, 0, 0);
        int kinks = 2 + level.getRandom().nextInt(effect.maxBranches());

        Vector3f segmentLength = end.sub(start, new Vector3f()).mul(1F/kinks);
        float deviation = effect.maxDeviation();

        List<Vector3f> nodes = new ArrayList<>();
        nodes.add(start);

        for (int i = 0; i < kinks - 1; i++) {
            start = start.add(segmentLength, new Vector3f()).add(
                    level.getRandom().triangle(0, deviation),
                    0,
                    level.getRandom().triangle(0, deviation)
            );
            nodes.add(start);
        }
        nodes.add(end);

        return new Bolt(true, nodes);
    }

    private Bolt generateBranch() {
        Vector3f startPos = new Vector3f(0, 0, 0);

        int intendedLength = 2 + level.getRandom().nextInt(effect.maxBranches());
        float deviation = effect.maxDeviation();
        List<Vector3f> nodes = new ArrayList<>();

        while (nodes.size() < intendedLength) {
            startPos = startPos.add(
                    level.getRandom().triangle(0F, deviation),
                    level.getRandom().triangle(0F, deviation),
                    level.getRandom().triangle(0F, deviation),
                    new Vector3f()
            );

            nodes.add(startPos);
        }

        return new Bolt(false, nodes);
    }

    @Override
    public void submitCustomGeometry(SubmitNodeCollector collector, Camera camera, float tickDelta) {
        Vec3 cam = camera.position();

        float x = (float)(Mth.lerp(tickDelta, xo, this.x) - cam.x());
        float y = (float)(Mth.lerp(tickDelta, yo, this.y) - cam.y());
        float z = (float)(Mth.lerp(tickDelta, zo, this.z) - cam.z());

        collector.submitCustomGeometry(new PoseStack(), RenderTypes.lightning(), (pose, buffer) -> {
            Vector3f origin = new Vector3f(x, y, z);
            Vector3f from = new Vector3f();
            Vector3f to = new Vector3f();

            for (Bolt branch : branches) {
                for (int i = 0; i < branch.nodes().size(); i++) {
                    renderBranch(buffer,
                            branch.isTrunk() ? 0.05125F : level.getRandom().nextFloat() / 30 + 0.01F,
                            i == 0 ? origin : branch.nodes().get(i - 1).add(x, y, z, from), branch.nodes().get(i).add(x, y, z, to)
                    );
                }
            }
        });
    }

    private void renderBranch(VertexConsumer buffer, float thickness, Vector3f from, Vector3f to) {
        renderQuad(buffer, new Vector3f[]{
            new Vector3f(from.x - thickness, from.y, from.z + thickness),
            new Vector3f(to.x - thickness, to.y, to.z + thickness),
            new Vector3f(to.x + thickness, to.y, to.z + thickness),
            new Vector3f(from.x + thickness, from.y, from.z + thickness),

            new Vector3f(from.x - thickness, from.y, from.z - thickness),
            new Vector3f(to.x - thickness, to.y, to.z - thickness),
            new Vector3f(to.x + thickness, to.y, to.z - thickness),
            new Vector3f(from.x + thickness, from.y, from.z - thickness),

            new Vector3f(from.x - thickness, from.y, from.z - thickness),
            new Vector3f(to.x - thickness, to.y, to.z - thickness),
            new Vector3f(to.x - thickness, to.y, to.z + thickness),
            new Vector3f(from.x - thickness, from.y, from.z + thickness),

            new Vector3f(from.x + thickness, from.y, from.z - thickness),
            new Vector3f(to.x + thickness, to.y, to.z - thickness),
            new Vector3f(to.x + thickness, to.y, to.z + thickness),
            new Vector3f(from.x + thickness, from.y, from.z + thickness)
            /*,

            new Vector3f(from.x - thickness, from.y - thickness * 2, from.z),
            new Vector3f(to.x - thickness, to.y - thickness * 2, to.z),
            new Vector3f(to.x + thickness, to.y - thickness * 2, to.z),
            new Vector3f(from.x + thickness, from.y - thickness * 2, from.z),

            new Vector3f(from.x, from.y - thickness, from.z + thickness),
            new Vector3f(to.x, to.y - thickness, to.z + thickness),
            new Vector3f(to.x, to.y + thickness, to.z + thickness),
            new Vector3f(from.x, from.y + thickness, from.z + thickness)

            ,

            new Vector3f(from.x - thickness * 2, from.y - thickness, from.z + thickness),
            new Vector3f(to.x - thickness * 2, to.y - thickness, to.z + thickness),
            new Vector3f(to.x - thickness * 2, to.y + thickness, to.z + thickness),
            new Vector3f(from.x - thickness * 2, from.y + thickness, from.z + thickness)*/
        }, 0.3F, 1);
    }

    record Bolt(boolean isTrunk, List<Vector3f> nodes) {

    }
}
