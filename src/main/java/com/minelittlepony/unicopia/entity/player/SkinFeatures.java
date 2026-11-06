package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkinFeatures(boolean showHorn, boolean showWings, Optional<Integer> skinColor) {
    public static final SkinFeatures DEFAULT = new SkinFeatures(true, true, Optional.empty());
    public static final Codec<SkinFeatures> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("show_horn").forGetter(SkinFeatures::showHorn),
            Codec.BOOL.fieldOf("show_wings").forGetter(SkinFeatures::showWings),
            Codec.INT.optionalFieldOf("skin_color").forGetter(SkinFeatures::skinColor)
    ).apply(i, SkinFeatures::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkinFeatures> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SkinFeatures::showHorn,
            ByteBufCodecs.BOOL, SkinFeatures::showWings,
            ByteBufCodecs.optional(ByteBufCodecs.INT), SkinFeatures::skinColor,
            SkinFeatures::new
    );
}
