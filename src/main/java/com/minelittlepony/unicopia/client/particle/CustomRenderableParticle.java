package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;

interface CustomRenderableParticle {
    void submitCustomGeometry(SubmitNodeCollector collector, Camera camera, float tickDelta);
}
