package com.minelittlepony.unicopia.client.render;

import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.FruitBearingBlock;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class LeavesAdditionsModel implements BlockStateModel {
    public static void bootstrap() {
        ModelLoadingPlugin.register(ctx -> {
            ctx.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
                if (!(context.state().getBlock() instanceof FruitBearingBlock) || model instanceof LeavesAdditionsModel) {
                    return model;
                }
                return new LeavesAdditionsModel(model);
            });
        });
    }

    private final BlockStateModel wrapped;

    private LeavesAdditionsModel(BlockStateModel model) {
        this.wrapped = model;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        emitter.pushTransform(quad -> {
            quad.chunkLayer(ChunkSectionLayer.CUTOUT);
            return true;
        });
        wrapped.emitQuads(emitter, blockView, pos, state, random, cullTest);
        emitter.popTransform();
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        wrapped.collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial() {
        return wrapped.particleMaterial();
    }

    @Override
    public int materialFlags() {
        return wrapped.materialFlags();
    }
}
