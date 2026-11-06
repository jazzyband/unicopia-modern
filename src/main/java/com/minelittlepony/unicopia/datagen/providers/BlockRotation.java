package com.minelittlepony.unicopia.datagen.providers;

import static net.minecraft.core.Direction.*;

import com.mojang.math.Quadrant;
import net.minecraft.core.Direction;

public class BlockRotation {
    private static final Quadrant[] ROTATIONS = Quadrant.values();
    public static final Direction[] DIRECTIONS = { EAST, SOUTH, WEST, NORTH };

    public static Quadrant cycle(Quadrant rotation, int steps) {
        int index = rotation.ordinal() + steps;
        while (index < 0) {
            index += ROTATIONS.length;
        }
        return ROTATIONS[index % ROTATIONS.length];
    }

    public static Quadrant next(Quadrant rotation) {
        return cycle(rotation, 1);
    }

    public static Quadrant previous(Quadrant rotation) {
        return cycle(rotation, -1);
    }
}
