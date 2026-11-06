package com.minelittlepony.unicopia.util;

import net.minecraft.world.InteractionResult;

public record TypedActionResult<T> (InteractionResult result, T value) {
    public static <T> TypedActionResult<T> success(T value) {
        return new TypedActionResult<>(InteractionResult.SUCCESS, value);
    }

    public static <T> TypedActionResult<T> pass(T value) {
        return new TypedActionResult<>(InteractionResult.PASS, value);
    }

    public static <T> TypedActionResult<T> fail(T value) {
        return new TypedActionResult<>(InteractionResult.FAIL, value);
    }

    public static <T> TypedActionResult<T> consume(T value) {
        return new TypedActionResult<>(InteractionResult.CONSUME, value);
    }
}
