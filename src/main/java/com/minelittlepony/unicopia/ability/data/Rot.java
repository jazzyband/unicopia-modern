package com.minelittlepony.unicopia.ability.data;

import com.minelittlepony.unicopia.EntityConvertable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record Rot (float pitch, float yaw) implements Hit {
    public static final StreamCodec<FriendlyByteBuf, Rot> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, Rot::pitch,
            ByteBufCodecs.FLOAT, Rot::yaw,
            Rot::new
    );

    public Rot applyTo(EntityConvertable<?> target) {
        Vec3 pos = target.getOriginVector();
        target.asEntity().absSnapTo(pos.x, pos.y, pos.z, yaw, pitch);
        return this;
    }

    public static Rot of(EntityConvertable<?> source) {
        return new Rot(source.asEntity().getViewXRot(1), source.asEntity().getYHeadRot());
    }
}