package com.minelittlepony.unicopia;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;

public enum Affinity implements StringRepresentable {
    GOOD(ChatFormatting.BLUE, -1, 0),
    NEUTRAL(ChatFormatting.LIGHT_PURPLE, 0, 0.5F),
    BAD(ChatFormatting.RED, 1, 1);

    public static final Codec<Affinity> CODEC = StringRepresentable.fromEnum(Affinity::values);

    private final ChatFormatting color;

    private final int corruption;
    private final float alignment;

    public static final Affinity[] VALUES = values();

    private final String translationKey;

    Affinity(ChatFormatting color, int corruption, float alignment) {
        this.color = color;
        this.corruption = corruption;
        this.alignment = alignment;
        this.translationKey = Util.makeDescriptionId("affinity", Unicopia.id(name().toLowerCase(Locale.ROOT)));
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public Component getDisplayName() {
        return Component.translatable(getTranslationKey()).withStyle(getColor());
    }

    public int getCorruption() {
        return corruption;
    }

    public float getAlignment() {
        return alignment;
    }

    public boolean isNeutral() {
        return this == NEUTRAL;
    }

    public boolean alignsWith(Affinity other) {
        return isNeutral() || other.isNeutral() || this == other;
    }

    public static Affinity of(int ordinal, Affinity fallback) {
        return ordinal < 0 || ordinal >= VALUES.length ? fallback : VALUES[ordinal];
    }
}
