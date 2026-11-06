package com.minelittlepony.unicopia.client.render.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.minelittlepony.unicopia.block.ItemJarBlock;
import com.minelittlepony.unicopia.block.jar.EntityJarContents;
import com.minelittlepony.unicopia.block.jar.FakeFluidJarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.FluidJarContents;
import com.minelittlepony.unicopia.block.jar.ItemsJarContents;
import com.minelittlepony.unicopia.client.render.model.CubeModel;
import com.minelittlepony.unicopia.util.FluidHelper;
import com.minelittlepony.unicopia.util.PosHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class ItemJarBlockEntityRenderer implements BlockEntityRenderer<ItemJarBlock.TileData, ItemJarBlockEntityRenderer.State> {
    private static final Direction[] GLASS_SIDES = Arrays.stream(PosHelper.ALL).filter(i -> i != Direction.UP).toArray(Direction[]::new);
    private final ItemModelResolver itemResolver;
    private final EntityRenderDispatcher dispatcher;

    public ItemJarBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        itemResolver = Minecraft.getInstance().getItemModelResolver();
        dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    public static class State extends BlockEntityRenderState {
        public final List<ItemStackRenderState> items = new ArrayList<>();
        public EntityRenderState containedEntity;
        public float entityScale;
        public float entityEyePos;
        public float tick;
        public TextureAtlasSprite topSprite;
        public TextureAtlasSprite sideSprite;
        public float fluidHeight;
        public int fluidColor;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ItemJarBlock.TileData data, State state, float tickDelta, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(data, state, tickDelta, cameraPos, breakProgress);
        state.items.clear();
        state.containedEntity = null;
        state.topSprite = null;
        state.sideSprite = null;
        state.tick = tickDelta;

        ItemsJarContents contents = data.getItems();
        if (contents != null) {
            for (ItemStack stack : contents.stacks()) {
                ItemStackRenderState item = new ItemStackRenderState();
                itemResolver.updateForTopItem(item, stack, ItemDisplayContext.FIXED, data.getLevel(), null, 0);
                state.items.add(item);
            }
        }

        EntityJarContents entity = data.getEntity();
        if (entity != null) {
            Entity e = entity.entity().get();
            if (e != null) {
                Player player = Minecraft.getInstance().player;
                float size = Math.max(e.getBbWidth(), e.getBbHeight());
                state.entityScale = 0.25F / Math.max(size, 0.01F);
                state.entityEyePos = e.getEyeHeight(e.getPose()) * state.entityScale;
                if (player != null) {
                    e.setPos(Vec3.atCenterOf(data.getBlockPos()));
                    e.lookAt(Anchor.FEET, Minecraft.getInstance().gameRenderer.mainCamera().position());
                }
                state.containedEntity = dispatcher.extractEntity(e, tickDelta);
            }
        }

        FluidJarContents fluid = data.getFluid();
        FakeFluidJarContents milk = data.getFakeFluid();
        if (fluid != null) {
            loadFluid(state, FluidHelper.getFullFluidState(fluid.fluid()), fluid.amount(), 0, data);
        } else if (milk != null) {
            loadFluid(state, Fluids.WATER.defaultFluidState(), FluidConstants.BUCKET, milk.color(), data);
        }
    }

    private void loadFluid(State state, FluidState fluid, long amount, int colorOverride, ItemJarBlock.TileData data) {
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid);
        state.topSprite = model.stillMaterial().sprite();
        state.sideSprite = model.flowingMaterial().sprite();
        state.fluidHeight = 0.6F * amount / (float)FluidConstants.BUCKET;
        state.fluidColor = colorOverride != 0 ? colorOverride : model.tintSource() == null
                ? CommonColors.WHITE : data.getLevel() instanceof net.minecraft.client.renderer.block.BlockAndTintGetter view
                        ? model.tintSource().colorInWorld(fluid.createLegacyBlock(), view, data.getBlockPos())
                        : model.tintSource().color(fluid.createLegacyBlock());
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector vertices, CameraRenderState camera) {
        if (!state.items.isEmpty()) {
            matrices.pushPose();
            matrices.translate(0.5, 0, 0.5);
            matrices.rotate(Axis.XP.rotationDegrees(90));
            matrices.scale(0.35F, 0.35F, 0.35F);
            RandomSource rng = RandomSource.create(state.blockPos.asLong());
            float y = 0;
            for (ItemStackRenderState item : state.items) {
                matrices.pushPose();
                matrices.translate((rng.nextFloat() - 0.5F) * 0.5F, (rng.nextFloat() - 0.5F) * 0.8F, -0.05 + y);
                matrices.rotate(Axis.XP.rotationDegrees(rng.nextFloat() * 360 - 180));
                matrices.rotate(Axis.ZP.rotationDegrees(rng.nextFloat() * 360 - 180));
                item.submit(matrices, vertices, state.lightCoords, 0, CommonColors.WHITE);
                matrices.popPose();
                y -= 0.1F;
            }
            matrices.popPose();
        }

        if (state.containedEntity != null) {
            matrices.pushPose();
            matrices.translate(0.5, 0.48 + Mth.sin(state.tick / 19F) * 0.02F - state.entityEyePos, 0.5);
            matrices.scale(state.entityScale, state.entityScale, state.entityScale);
            matrices.rotate(Axis.XP.rotationDegrees(10 * Mth.sin(state.tick / 19F)));
            dispatcher.submit(state.containedEntity, camera, 0, 0, 0, matrices, vertices);
            matrices.popPose();
        }

        if (state.topSprite != null && state.sideSprite != null) {
            submitFluidFace(vertices, matrices, state.topSprite, state, new Direction[] {Direction.UP});
            submitFluidFace(vertices, matrices, state.sideSprite, state, GLASS_SIDES);
        }
    }

    private void submitFluidFace(SubmitNodeCollector vertices, PoseStack matrices, TextureAtlasSprite sprite,
            State state, Direction[] sides) {
        RenderType layer = ARGB.alphaFloat(state.fluidColor) >= 1
                ? RenderTypes.entitySolid(sprite.atlasLocation()) : RenderTypes.entityTranslucent(sprite.atlasLocation());
        vertices.submitCustomGeometry(matrices, layer, (pose, buffer) -> {
            PoseStack stack = new PoseStack();
            stack.mulPose(pose.pose());
            CubeModel.render(stack, buffer, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                    0.28F, 0.01F, 0.28F, 0.73F, 0.01F + state.fluidHeight, 0.73F,
                    state.fluidColor | 0xFF000000, state.lightCoords, 0, sides);
        });
    }
}
