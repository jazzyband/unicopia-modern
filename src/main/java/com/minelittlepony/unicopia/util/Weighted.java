package com.minelittlepony.unicopia.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public final class Weighted {
    private record Tuple<A, B>(A getA, B getB) {}
    private static final Supplier<Optional<?>> EMPTY = Optional::empty;

    @SuppressWarnings("unchecked")
    public static <T> Supplier<Optional<T>> of() {
        return (Supplier<Optional<T>>)(Object)EMPTY;
    }

    public static <T> Supplier<Optional<T>> of(Consumer<Weighted.Builder<T>> constructor) {
        Weighted.Builder<T> result = new Weighted.Builder<>();
        constructor.accept(result);
        return result.build();
    }

    public static <T> Supplier<Optional<T>> of(Collection<? extends Buildable<T>> entries) {
        return of(builder -> entries.forEach(entry -> entry.appendTo(builder)));
    }

    public static RandomSource getRng() {
        return Builder.RANDOM;
    }

    public final static class Builder<T> {
        public static final RandomSource RANDOM = RandomSource.create();

        private float totalWeight = 0;

        private final List<Tuple<WeightedValue<T>, Range>> entries = new ArrayList<>();

        public Builder<T> putAll(Map<Integer, T> map) {
            map.forEach(this::put);
            return this;
        }

        public Builder<T> put(int weight, @NotNull T value) {
            entries.add(new Tuple<>(new WeightedValue<>(weight, value), new Range()));

            totalWeight += weight;

            float rangeStart = 0;

            for (var i : entries) {
                rangeStart = i.getB().set(rangeStart, (i.getA().weight() / totalWeight));
            }

            return this;
        }

        public Supplier<Optional<T>> build() {
            if (entries.isEmpty()) {
                return of();
            }
            if (entries.size() == 1) {
                final var val = Optional.ofNullable(entries.get(0).getA().result());
                return () -> val;
            }
            final var entries = new ArrayList<>(this.entries);
            return () -> {
                final float pointer = RANDOM.nextFloat();
                return entries.stream()
                        .filter(i -> i.getB().isIn(pointer))
                        .map(i -> i.getA().result())
                        .findFirst();
            };
        }

        private record WeightedValue<T> (float weight, T result) {}

        private final class Range {
            float min;
            float max;

            public boolean isIn(float pointer) {
                return pointer >= min && pointer <= max;
            }

            public float set(float start, float size) {
                min = start;
                max = start + size;
                return max;
            }
        }
    }

    public interface Buildable<T> {
        void appendTo(Weighted.Builder<T> weighted);
    }
}
