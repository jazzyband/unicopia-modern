package com.minelittlepony.unicopia.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.multipart.Condition;
import net.minecraft.client.renderer.block.dispatch.multipart.Selector;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import com.minelittlepony.unicopia.util.Untyped;

public interface IndirectionUtils {

    static <T> Holder<T> entryOf(ResourceKey<Registry<T>> registry, Identifier value, T dummyValue) {
        final Registry<T> registryRef = Untyped.cast(BuiltInRegistries.REGISTRY.getValue(registry.identifier()));
        class Entry extends Holder.Reference<T> {
            Entry() {
                super(
                    Holder.Reference.Type.STAND_ALONE,
                    registryRef,
                    ResourceKey.create(registry, value),
                    dummyValue
                );
            }
        }
        return new Entry();
    }

    static IndirectMultipartBlockStateSupplier multipartBlockStateSupplier(Identifier block) {
        return new IndirectMultipartBlockStateSupplier(block);
    }

    public class IndirectMultipartBlockStateSupplier implements BlockModelDefinitionGenerator, DataCollector.Identifiable {
        private final List<Part> parts = new ArrayList<>();

        private final Identifier block;

        private IndirectMultipartBlockStateSupplier(Identifier block) {
            this.block = block;
        }

        @Override
        public Block block() {
            throw new RuntimeException("Stub");
        }

        @Override
        public Identifier getId() {
            return block;
        }

        public IndirectMultipartBlockStateSupplier with(MultiVariant variant) {
            parts.add(new Part(Optional.empty(), variant));
            return this;
        }

        public IndirectMultipartBlockStateSupplier with(Condition condition, MultiVariant variant) {
            parts.add(new Part(Optional.of(condition), variant));
            return this;
        }

        public IndirectMultipartBlockStateSupplier with(ConditionBuilder conditionBuilder, MultiVariant part) {
            return with(conditionBuilder.build(), part);
        }

        @Override
        public BlockStateModelDispatcher create() {
            return new BlockStateModelDispatcher(
                Optional.empty(),
                Optional.of(new BlockStateModelDispatcher.MultiPartDefinition(parts.stream().map(Part::toComponent).toList()))
            );
        }

        record Part(Optional<Condition> condition, MultiVariant variants) {
            public Selector toComponent() {
                return new Selector(condition, variants.toUnbaked());
            }
        }
    }
}
