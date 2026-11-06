package com.minelittlepony.unicopia.ability.data;

import com.minelittlepony.unicopia.ability.magic.Caster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record Pos (int x, int y, int z) implements Hit {
    public static final StreamCodec<FriendlyByteBuf, Pos> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, Pos::x,
                ByteBufCodecs.INT, Pos::y,
                ByteBufCodecs.INT, Pos::z,
                Pos::new
    );

    public Pos(Vec3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos pos() {
        return new BlockPos(x, y, z);
    }

    public Vec3 vec() {
        return Vec3.atCenterOf(new Vec3i(x, y, z));
    }

    public double distanceTo(Caster<?> caster) {
        return Math.sqrt(caster.asEntity().distanceToSqr(x, y, z));
    }
}