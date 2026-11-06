package com.minelittlepony.unicopia.command;

import net.minecraft.util.StringRepresentable;

public interface CommandArgumentEnum<T extends Enum<T> & CommandArgumentEnum<T>> extends StringRepresentable {

    @SuppressWarnings("unchecked")
    default T asSelf() {
        return (T)this;
    }

    @Override
    default String getSerializedName() {
        return asSelf().name();
    }
}
