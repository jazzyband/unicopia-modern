package com.minelittlepony.unicopia.server.world.gen;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.mixin.server.MixinStructurePool;
import com.minelittlepony.unicopia.mixin.server.MixinStructureProcessorList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.core.registries.Registries;

public interface StructureExtensions {
    static void bootstrap() {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.registerEntryAdded(Registries.TEMPLATE_POOL, new DynamicEntryMerger<>(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, biMapped(MixinStructurePool.class::cast, (source, target) -> {
                target.setTemplates(new ObjectArrayList<>(union(target.getTemplates(), source.getTemplates())));
                target.setElementCounts(union(target.getElementCounts(), source.getElementCounts()));
            })));

            registries.registerEntryAdded(Registries.PROCESSOR_LIST, new DynamicEntryMerger<>(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, (source, target) -> {
                ((MixinStructureProcessorList)target).setList(union(source.list(), target.list()));
            }));
        });
    }

    static <T> List<T> union(List<T> a, List<T> b) {
        return Stream.concat(a.stream(), b.stream()).toList();
    }

    static <T, X> BiConsumer<T, T> biMapped(Function<T, X> remapper, BiConsumer<X, X> consumer) {
        return (a, b) -> consumer.accept(remapper.apply(a), remapper.apply(b));
    }
}