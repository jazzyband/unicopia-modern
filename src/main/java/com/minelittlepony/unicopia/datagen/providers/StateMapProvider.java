package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.block.state.ReversableBlockStateConverter;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class StateMapProvider implements DataProvider {
    private final DataCollector<Entry> collector;

    public StateMapProvider(FabricPackOutput output) {
        collector = new DataCollector<>(output.createPathProvider(PackOutput.Target.DATA_PACK, "state_maps"), Entry.CODEC);
    }

    @Override
    public String getName() {
        return "Block State Maps";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        var exporter = collector.prime();
        new StateMapGenerator().generate((id, builder) -> exporter.accept(id.getId(), new Entry(builder.build(), false)));
        return collector.upload(writer);
    }

    record Entry(ReversableBlockStateConverter entries, boolean replace) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ReversableBlockStateConverter.codec().fieldOf("entries").forGetter(Entry::entries),
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(Entry::replace)
        ).apply(i, Entry::new));
    }
}
