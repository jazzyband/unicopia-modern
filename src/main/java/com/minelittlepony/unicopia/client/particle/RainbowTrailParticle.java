package com.minelittlepony.unicopia.client.particle;

import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.bezier.BezierSegment;
import com.minelittlepony.unicopia.client.render.bezier.Trail;
import com.minelittlepony.unicopia.particle.TargetBoundParticleEffect;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class RainbowTrailParticle extends AbstractBillboardParticle {
    private static final Identifier TEXTURE = Unicopia.id("textures/particles/rainboom_trail.png");

    private final BezierSegment bezier = new BezierSegment();

    private final Trail trail;

    @Nullable
    private Entity target;
    private boolean isAbility;

    public RainbowTrailParticle(TargetBoundParticleEffect effect, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        trail = new Trail(new Vec3(x, y, z), 1);
        setLifetime(300);
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        if (effect.targetId() <= 0) {
            this.target = world.getEntities(null, AABB.unitCubeFromLowerCorner(trail.pos)).get(0);
        } else {
            this.target = world.getEntity(effect.targetId());
        }
        isAbility = Caster.of(target).filter(caster -> SpellType.RAINBOOM.isOn(caster)).isPresent();
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

        if (target != null && target.isAlive()) {
            if (isAbility) {
                age = 0;
            }
            trail.update(target.getEyePosition());

            if (isAbility && Caster.of(target).filter(caster -> SpellType.RAINBOOM.isOn(caster)).isEmpty()) {
                target = null;
            }
        }

        if (trail.tick()) {
            remove();
        }
    }
}
