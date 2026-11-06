package com.minelittlepony.unicopia.client.render.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SpellEffectsRenderDispatcher implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {
    public static final SpellEffectsRenderDispatcher INSTANCE = new SpellEffectsRenderDispatcher();
    private static final Identifier ID = Unicopia.id("spell_renderers");
    private static final Map<SpellType<?>, SpellRendererFactory<?>> REGISTRY = new HashMap<>();

    public static <T extends Spell> void register(SpellType<T> type, SpellRendererFactory<? super T> rendererFactory) {
        REGISTRY.put(type, rendererFactory);
    }

    static {
        register(SpellType.SHIELD, ShieldSpellRenderer::new);
        register(SpellType.DARK_VORTEX, DarkVortexSpellRenderer::new);
        register(SpellType.BUBBLE, BubbleSpellRenderer::new);
        register(SpellType.PORTAL, PortalSpellRenderer::new);
        register(SpellType.RAINBOOM, RainboomSpellRenderer::new);
    }

    @Nullable
    private Map<SpellType<?>, SpellRenderer<?, ?>> renderers = Map.of();
    private final Minecraft client = Minecraft.getInstance();

    private SpellEffectsRenderDispatcher() {}

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @SuppressWarnings("unchecked")
    public <S extends Spell> SpellRenderer<S, ?> getRenderer(S spell) {
        return (SpellRenderer<S, ?>)renderers.getOrDefault(spell.getTypeAndTraits().type(), SpellRenderer.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public <S extends SpellRenderState> SpellRenderer<?, S> getRenderer(S spell) {
        Objects.requireNonNull(spell.type);
        return (SpellRenderer<?, S>)renderers.getOrDefault(spell.type.type(), SpellRenderer.DEFAULT);
    }

    public void render(PoseStack matrices, SubmitNodeCollector vertices, int light, CasterState caster, SpellRenderState spell) {
        var renderer = getRenderer(spell);

        if (renderer != SpellRenderer.DEFAULT) {
            renderer.render(matrices, vertices, spell, caster, light);
        }
    }

    public void render(PoseStack matrices, SubmitNodeCollector vertices, int light, CasterState caster) {
        if (!((RenderDispatcherAccessor)client.getEntityRenderDispatcher()).shouldRenderShadows()) {
            return;
        }

        caster.spells.forEach(spell -> {
            render(matrices, vertices, light, caster, spell);
        });

        if (caster.debugLines != null) {
            renderHotspot(matrices, vertices, caster);
            renderSpellDebugInfo(matrices, vertices, caster, light);
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        renderers = REGISTRY.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().create()));
    }

    private void renderSpellDebugInfo(PoseStack matrices, SubmitNodeCollector vertices, CasterState caster, int light) {
        // Debug labels require a separate extracted text submission path.
    }

    private void renderHotspot(PoseStack matrices, SubmitNodeCollector vertices, CasterState caster) {
        // Debug geometry is intentionally omitted until the 26.3 line submission is migrated.
    }

    public interface RenderDispatcherAccessor {
        boolean shouldRenderShadows();
    }
}
