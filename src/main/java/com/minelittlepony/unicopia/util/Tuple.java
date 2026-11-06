package com.minelittlepony.unicopia.util;

/** A pair of values used by Unicopia's recipes and data generators. */
public record Tuple<A, B>(A getA, B getB) {
}
