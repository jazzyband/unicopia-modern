package com.minelittlepony.unicopia.client.particle;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.bezier.BezierSegment;
import com.minelittlepony.unicopia.client.render.bezier.Trail;
import com.minelittlepony.unicopia.particle.TargetBoundParticleEffect;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class WindParticle extends AbstractBillboardParticle {
    private static final Identifier TEXTURE = Unicopia.id("textures/particle/wind.png");

    private final BezierSegment bezier = new BezierSegment();

    private final Trail trail;

    @Nullable
    private Entity target;

    private int attachmentTicks;

    private final Vec3 offset;
    private final boolean passive;

    public WindParticle(TargetBoundParticleEffect effect, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        trail = new Trail(new Vec3(x, y, z), 0.02F);
        setLifetime(300);
        this.alpha = 0.15F;
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
        this.attachmentTicks = (int)world.getRandom().triangle(15, 12);
        this.passive = effect.targetId() <= 0;
        this.hasPhysics = false;

        if (effect.targetId() > 0) {
            this.target = world.getEntity(effect.targetId());
        }
        offset = target == null ? Vec3.ZERO : new Vec3(x, y, z).subtract(target.position());
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public boolean isAlive() {
        return age < getLifetime() && (!removed || !trail.getSegments().isEmpty());
    }

    @Override
    protected void renderQuads(VertexConsumer buffer, float x, float y, float z, float tickDelta) {
        float alpha = this.alpha * (1 - (float)age / lifetime);
        int light = getLightCoords(tickDelta);
        float scale = getScale(tickDelta);

        List<Trail.Segment> segments = trail.getSegments();

        for (int i = 0; i < segments.size() - 1; i++) {
            segments.get(i).getPlane(segments.get(i + 1), bezier);

            for (var corner : bezier.corners()) {
                corner.position().mul(scale).add(x, y, z);
            }

            quad(buffer, bezier.corners(), segments.get(i).getAlpha() * alpha, tickDelta, light);
        }
    }

    @Override
    public void tick() {
        super.tick();

        float animationFrame = age + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        float sin = Mth.sin(animationFrame / 5F) * 0.1F;
        float cos = Mth.cos(animationFrame / 10F) * 0.2F;

        if (passive) {
            trail.update(new Vec3(x + cos, y + sin, z - cos));
        } else {
            if (target != null && target.isAlive()) {

                trail.update(target.position().add(target.getForward().scale(-7)).add(offset).add(cos, sin, -cos));

                if (attachmentTicks > 0 && --attachmentTicks <= 0) {
                    target = null;
                }
            }
        }

        if (trail.tick()) {
            remove();
        }
    }
}
