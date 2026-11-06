package com.minelittlepony.unicopia.particle;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WeakTarget {
    public static final Codec<WeakTarget> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(instance -> instance.group(
                    CodecUtils.VECTOR.fieldOf("position").forGetter(i -> i.fixedPosition),
                    Codec.INT.fieldOf("targetId").forGetter(i -> i.targetId)
            ).apply(instance, WeakTarget::new)),
            CodecUtils.VECTOR,
            pos -> new WeakTarget(pos, null)
    );
    public static final StreamCodec<FriendlyByteBuf, WeakTarget> PACKET_CODEC = StreamCodec.composite(
            PacketCodecUtils.VECTOR, i -> i.fixedPosition,
            ByteBufCodecs.INT, i -> i.targetId,
            WeakTarget::new
    );

    Vec3 fixedPosition;
    private int targetId;

    private WeakTarget(Vec3 fixedPosition, int targetId) {
        this.fixedPosition = fixedPosition;
        this.targetId = targetId;
    }

    public WeakTarget(Vec3 fixedPosition, @Nullable Entity entity) {
        this(fixedPosition, entity == null ? -1 : entity.getId());
    }

    public Vec3 getPosition(Level world) {
        if (targetId > -1) {
            Entity e = world.getEntity(targetId);
            if (e != null) {
                fixedPosition = e.getEyePosition(1);
            } else {
                targetId = -1;
            }
        }
        return fixedPosition;
    }

    @Override
    public String toString() {
        if (targetId > -1) {
            return "Moving(" + targetId + ")";
        }
        return "Fixed(" + fixedPosition + ")";
    }
}
