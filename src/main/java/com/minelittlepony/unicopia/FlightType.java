package com.minelittlepony.unicopia;

import java.util.Locale;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import com.mojang.serialization.Codec;

public enum FlightType implements StringRepresentable {
    UNSET,
    NONE,
    AVIAN,
    INSECTOID,
    ARTIFICIAL;

    public static final Codec<FlightType> CODEC = StringRepresentable.fromEnum(FlightType::values);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean isGrounded() {
        return this == NONE;
    }

    public boolean isAvian() {
        return this == AVIAN || isArtifical();
    }

    public boolean isArtifical() {
        return this == ARTIFICIAL;
    }

    public boolean isPresent() {
        return this != UNSET;
    }

    public boolean canFly(Player player) {
        return !isGrounded() || player.getAbilities().mayfly;
    }

    public static boolean canFlyCreative(Player player) {
        return player.isCreative() || player.isSpectator();
    }

    public SoundEvent getWingFlapSound() {
        return this == INSECTOID ? USounds.ENTITY_PLAYER_CHANGELING_BUZZ : USounds.ENTITY_PLAYER_PEGASUS_WINGSFLAP;
    }

    public float getWingFlapSoundPitch() {
        return this == INSECTOID ? 0.66F : 1;
    }

    public FlightType or(FlightType other) {
        return ordinal() > other.ordinal() ? this : other;
    }

    /**
     * Predicate for abilities to control whether a player can fly.
     *
     * This overrides what the race specifies.
     */
    public interface Provider {
        FlightType getFlightType();
    }
}
