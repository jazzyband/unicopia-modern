package com.minelittlepony.unicopia;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import com.mojang.serialization.Codec;

public enum Availability implements StringRepresentable {
    DEFAULT,
    COMMANDS,
    NONE;

    public static final Codec<Availability> CODEC = StringRepresentable.fromEnum(Availability::values);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean isSelectable() {
        return this == DEFAULT;
    }

    public boolean isGrantable() {
        return this != NONE;
    }
}
