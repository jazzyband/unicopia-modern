package com.minelittlepony.unicopia.client.particle;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.particle.v1.ParticleGroupRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

/** Collects Unicopia's non-billboard particle geometry for the 26.3 submit stage. */
public final class CustomParticleGroup extends ParticleGroup<Particle> {
    public static final ParticleRenderType RENDER_TYPE = new ParticleRenderType("unicopia:custom", "unicopia_custom");

    public static void register() {
        ParticleGroupRegistry.register(RENDER_TYPE, CustomParticleGroup::new);
        ParticleGroupRegistry.registerOrdering(ParticleRenderType.SINGLE_QUADS, RENDER_TYPE);
    }

    private CustomParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float tickDelta) {
        List<CustomRenderableParticle> visible = new ArrayList<>();
        for (Particle particle : particles) {
            if (particle instanceof CustomRenderableParticle custom) {
                visible.add(custom);
            }
        }
        return new State(List.copyOf(visible), camera, tickDelta);
    }

    private record State(List<CustomRenderableParticle> particles, Camera camera, float tickDelta) implements ParticleGroupRenderState {
        @Override
        public void submit(SubmitNodeCollector collector, CameraRenderState cameraState) {
            for (CustomRenderableParticle particle : particles) {
                particle.submitCustomGeometry(collector, camera, tickDelta);
            }
        }
    }
}
