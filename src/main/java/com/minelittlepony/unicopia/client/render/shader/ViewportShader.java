package com.minelittlepony.unicopia.client.render.shader;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.client.PostChainAccessor;
import com.minelittlepony.unicopia.mixin.client.PostPassAccessor;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ViewportShader {
    public static final ViewportShader INSTANCE = new ViewportShader();

    public static final Identifier CREEPER_SHADER = Identifier.withDefaultNamespace("shaders/post/creeper.json");
    public static final Identifier DESATURATION_SHADER = Unicopia.id("desaturate");

    private final Minecraft client = Minecraft.getInstance();
    private ByteBuffer saturationData;

    public void render(CrossFrameResourcePool pool, float tickDelta) {
        if (Unicopia.getConfig().disableShaders.get()) {
            return;
        }

        Pony pony = Pony.of(client.player);

        if (pony != null) {
            float corruption = pony.getInterpolator().interpolate("corruption", pony.getCorruption().getScaled(0.9F), 10);
            if (!Mth.equal(corruption, 0)) {
                PostChain processor = client.getShaderManager().getPostChain(DESATURATION_SHADER, LevelTargetBundle.MAIN_TARGETS);
                if (processor == null) {
                    return;
                }

                var passes = ((PostChainAccessor)processor).unicopia$getPasses();
                var uniform = ((PostPassAccessor)passes.getFirst()).unicopia$getCustomUniforms().get("DesaturationConfig");
                if (uniform == null) {
                    return;
                }
                if (saturationData == null || saturationData.capacity() != uniform.size()) {
                    saturationData = ByteBuffer.allocateDirect((int)uniform.size()).order(ByteOrder.nativeOrder());
                }
                saturationData.clear();
                saturationData.putFloat(Mth.clamp(1 - corruption + 0.05F, 0, 1));
                saturationData.rewind();
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uniform.slice(), saturationData);
                processor.process(client.gameRenderer.mainRenderTarget(), pool);
            }
        }
    }
}
