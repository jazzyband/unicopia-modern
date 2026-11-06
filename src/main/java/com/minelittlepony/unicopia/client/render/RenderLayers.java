package com.minelittlepony.unicopia.client.render;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.minelittlepony.unicopia.client.render.shader.URenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;

public final class RenderLayers {
    public static final int DEFAULT_MAGIC_COLOR = ARGB.colorFromFloat(0.6F, 0.8F, 0.9F, 1);
    private static final List<RenderType> BLOCK_DESTRUCTION_STAGE_LAYERS = ModelBakery.DESTROY_TYPES;

    private static final RenderType MAGIC_NO_COLOR = RenderType.create("unicopia_magic_no_color",
            RenderSetup.builder(URenderPipelines.TRANSLUCENT_SOLID).sortOnUpload().createRenderSetup());
    private static final RenderType MAGIC_SHIELD = RenderType.create("unicopia_magic_shield",
            RenderSetup.builder(URenderPipelines.TRANSLUCENT_SOLID_NO_CULL).sortOnUpload().createRenderSetup());
    private static final Function<Integer, RenderType> MAGIC_COLOR_FUNC = Util.memoize(color ->
            RenderType.create("unicopia_magic_colored_" + color, RenderSetup.builder(URenderPipelines.TRANSLUCENT_SOLID_NO_CULL)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).sortOnUpload().createRenderSetup()));
    private static final BiFunction<Identifier, Integer, RenderType> MAGIC_TINT_FUNC = Util.memoize((texture, color) ->
            RenderType.create("unicopia_magic_tint_" + color, RenderSetup.builder(URenderPipelines.TRANSLUCENT_SOLID_NO_CULL)
                    .withTexture("Sampler0", texture).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .sortOnUpload().createRenderSetup()));
    private static final Function<Identifier, RenderType> PORTAL = Util.memoize(texture ->
            RenderType.create("unicopia_portal", RenderSetup.builder(URenderPipelines.PORTAL_SURFACE)
                    .withTexture("Sampler0", texture).createRenderSetup()));
    public static RenderType getCrumbling(int stage) { return BLOCK_DESTRUCTION_STAGE_LAYERS.get(stage); }
    public static RenderType getMagicNoColor() { return MAGIC_NO_COLOR; }
    public static RenderType getMagicShield() { return MAGIC_SHIELD; }
    public static RenderType getMagicColored() { return getMagicColored(DEFAULT_MAGIC_COLOR); }
    public static RenderType getMagicColored(int color) { return MAGIC_COLOR_FUNC.apply(color); }
    public static RenderType getMagicColored(Identifier texture, int color) { return MAGIC_TINT_FUNC.apply(texture, color); }
    public static RenderType getPortal(Identifier texture) { return PORTAL.apply(texture); }

    private RenderLayers() {}
}
