package com.minelittlepony.unicopia.server.world.gen;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.Parameter;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import com.minelittlepony.unicopia.server.world.gen.BiomeSelectionContext.SplittableBiomeCoordinate;
import com.mojang.datafixers.util.Pair;

public final class BiomeSelectionInjector implements Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> {

    private final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameterConsumer;

    public BiomeSelectionInjector(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameterConsumer) {
        this.parameterConsumer = parameterConsumer;
    }

    @Override
    public void accept(Pair<ParameterPoint, ResourceKey<Biome>> parameter) {
        Context context = new Context(parameter);
        OverworldBiomeSelectionCallback.EVENT.invoker().onSelectingBiome(context);
        if (!context.overrides.isEmpty()) {
            List<Pair<ParameterPoint, ResourceKey<Biome>>> subSections = List.of(parameter);
            // sort splits from largest to smallest
            var divisions = context.overrides.entrySet()
                .stream()
                .sorted(Comparator.<Map.Entry<SplittableBiomeCoordinate, ResourceKey<Biome>>, Float>comparing(entry -> Context.getVolume(entry.getKey())).reversed())
                .toList();
            // recursively sub-divide into parts so every split gets a (kinda) fair share
            for (Map.Entry<SplittableBiomeCoordinate, ResourceKey<Biome>> division : divisions) {
                subSections = subSections.stream().flatMap(par -> Stream.of(
                        Pair.of(Context.write(division.getKey(), par.getFirst()), division.getValue()),
                        par
                )).toList();
            }
            // output the result
            subSections.forEach(parameterConsumer);
        } else {
            parameterConsumer.accept(parameter);
        }
    }

    class Context implements BiomeSelectionContext {
        private final Pair<ParameterPoint, ResourceKey<Biome>> parameter;
        private final Map<SplittableBiomeCoordinate, ResourceKey<Biome>> overrides = new LinkedHashMap<>();

        public Context(Pair<ParameterPoint, ResourceKey<Biome>> parameter) {
            this.parameter = parameter;
        }

        @Override
        public ResourceKey<Biome> biomeKey() {
            return parameter.getSecond();
        }

        @Override
        public SplittableBiomeCoordinate referenceFrame() {
            return createStart(parameter.getFirst());
        }

        @Override
        @Nullable
        public ResourceKey<Biome> addOverride(SplittableBiomeCoordinate coordinate, ResourceKey<Biome> biome) {
            return overrides.put(coordinate, biome);
        }

        static float getVolume(SplittableBiomeCoordinate coordinate) {
            return ((SplitableParameterRangeImpl)coordinate.temperature()).length()
                    * ((SplitableParameterRangeImpl)coordinate.humidity()).length()
                    * ((SplitableParameterRangeImpl)coordinate.continentalness()).length()
                    * ((SplitableParameterRangeImpl)coordinate.erosion()).length()
                    * ((SplitableParameterRangeImpl)coordinate.depth()).length()
                    * ((SplitableParameterRangeImpl)coordinate.weirdness()).length();
        }

        static ParameterPoint write(SplittableBiomeCoordinate coordinate, ParameterPoint referenceFrame) {
            return new ParameterPoint(
                    ((SplitableParameterRangeImpl)coordinate.temperature()).write(referenceFrame.temperature()),
                    ((SplitableParameterRangeImpl)coordinate.humidity()).write(referenceFrame.humidity()),
                    ((SplitableParameterRangeImpl)coordinate.continentalness()).write(referenceFrame.continentalness()),
                    ((SplitableParameterRangeImpl)coordinate.erosion()).write(referenceFrame.erosion()),
                    ((SplitableParameterRangeImpl)coordinate.depth()).write(referenceFrame.depth()),
                    ((SplitableParameterRangeImpl)coordinate.weirdness()).write(referenceFrame.weirdness()),
                    coordinate.offset()
            );
        }

        static SplittableBiomeCoordinate createStart(ParameterPoint referenceFrame) {
            final SplittableBiomeCoordinate[] self = new SplittableBiomeCoordinate[1];
            return self[0] = new SplittableBiomeCoordinate(
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::temperature, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::humidity, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::continentalness, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::erosion, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::depth, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::weirdness, 0, 1),
                    referenceFrame.offset()
            );
        }

        static SplittableBiomeCoordinate createCopy(SplittableBiomeCoordinate old) {
            final SplittableBiomeCoordinate[] self = new SplittableBiomeCoordinate[1];
            return self[0] = new SplittableBiomeCoordinate(
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.temperature()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.humidity()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.continentalness()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.erosion()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.depth()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.weirdness()),
                    old.offset()
            );
        }

        private static final class SplitableParameterRangeImpl implements SplitableParameterRange {
            private final SplittableBiomeCoordinate[] coordinate;
            private final Function<SplittableBiomeCoordinate, SplitableParameterRange> dimension;
            private float min;
            private float max;

            SplitableParameterRangeImpl(SplittableBiomeCoordinate[] coordinate, SplitableParameterRangeImpl original) {
                this(coordinate, original.dimension, original.min, original.max);
            }

            SplitableParameterRangeImpl(SplittableBiomeCoordinate[] coordinate, Function<SplittableBiomeCoordinate, SplitableParameterRange> dimension, float min, float max) {
                this.coordinate = coordinate;
                this.dimension = dimension;
                this.min = min;
                this.max = max;
            }

            @Override
            public SplittableBiomeCoordinate splitAbove(float midpoint) {
                return copyWithDifference(o -> o.min = Mth.lerp(midpoint, o.min, o.max));
            }

            @Override
            public SplittableBiomeCoordinate splitBelow(float midpoint) {
                return copyWithDifference(o -> o.max = Mth.lerp(midpoint, o.min, o.max));
            }

            private SplittableBiomeCoordinate copyWithDifference(Consumer<SplitableParameterRangeImpl> mutator) {
                SplittableBiomeCoordinate copy = createCopy(coordinate[0]);
                mutator.accept((SplitableParameterRangeImpl)dimension.apply(copy));
                return copy;
            }

            public Parameter write(Parameter range) {
                return Parameter.span(
                        Climate.unquantizeCoord(range.min()) * (1 + min),
                        Climate.unquantizeCoord(range.max()) * max
                );
            }

            public float length() {
                return max - min;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof SplitableParameterRangeImpl i
                        && Mth.equal(i.min, min)
                        && Mth.equal(i.max, max);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(min, max);
            }
        }
    }
}
