package com.minelittlepony.unicopia.client.render.item;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import com.minelittlepony.unicopia.item.component.Appearance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class JarContentsModelRenderer implements SpecialModelRenderer<ItemStack> {
    @Override
    public void submit(@Nullable ItemStack appearance, PoseStack matrices, SubmitNodeCollector vertices,
            int light, int overlay, boolean glint, int color) {
        if (appearance == null || appearance.isEmpty()) return;
        ItemStackRenderState item = new ItemStackRenderState();
        Minecraft client = Minecraft.getInstance();
        client.getItemModelResolver().updateForTopItem(item, appearance, ItemDisplayContext.FIXED, client.level, null, 0);
        matrices.pushPose();
        matrices.scale(0.5F, 0.5F, 0.5F);
        item.submit(matrices, vertices, light, overlay, color);
        matrices.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        extents.accept(new Vector3f(-0.5F, -0.5F, -0.5F));
        extents.accept(new Vector3f(0.5F, 0.5F, 0.5F));
    }

    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return Appearance.hasAppearance(stack) ? Appearance.upwrapAppearance(stack) : null;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
            return new JarContentsModelRenderer();
        }
    }
}