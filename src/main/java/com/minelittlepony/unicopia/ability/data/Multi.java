package com.minelittlepony.unicopia.ability.data;

import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Multi (Pos pos, int hitType) implements Hit {
    public static final StreamCodec<FriendlyByteBuf, Multi> CODEC = StreamCodec.composite(
            Pos.CODEC, Multi::pos,
            ByteBufCodecs.INT, Multi::hitType,
            Multi::new
    );

    public Multi(Vec3i pos, int hit) {
        this(new Pos(pos), hit);
    }
}