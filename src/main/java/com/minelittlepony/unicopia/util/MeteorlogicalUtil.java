package com.minelittlepony.unicopia.util;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.HitResult.Type;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;

public interface MeteorlogicalUtil {
    float SUNRISE = 0;
    float NOON = 0.5F;
    float SUNSET = 1;
    float MIDNIGHT = 1.5F;
    float MOONSET = 2;

    /**
     * @see net.minecraft.world.level.dimension.DimensionType#MOON_BRIGHTNESS_PER_PHASE
     */
    int FULL_MOON = 0;

    static int getMoonPhase(Level world) {
        return (int)Math.floorMod(world.getOverworldClockTime() / 24000L, 8);
    }

    static boolean isBetween(float skyAngle, float start, float end) {
        return skyAngle >= start && skyAngle <= end;
    }

    static boolean isCloseTo(float skyAngle, float value, float deviation) {
        return isBetween(skyAngle, value - deviation, value + deviation);
    }

    static boolean isLookingIntoSun(Level world, Entity entity) {

        // check first whether the world has a sun
        if (!world.dimensionType().hasSkyLight()) {
            return false;
        }

        // check if sun is obscured by clouds
        if (world.isRainingAt(entity.blockPosition()) || world.isThundering()) {
            return false;
        }

        final float skyAngle = getSkyAngle(entity.level());
        float playerYaw = Mth.wrapDegrees(entity.getYHeadRot());
        float playerAngle = (-entity.getViewXRot(1) / 90F) / 2F;

        // player is facing the other way so flip the yaw to match sun angle
        if (playerYaw > 0) {
            playerAngle = 1 - playerAngle;
        }

        if (world.isClientSide()) {
            playerYaw += UnicopiaClient.getInstance().tangentalSkyAngle.getValue();
        } else {
            playerYaw += UnicopiaWorldProperties.forWorld((ServerLevel)world).getTangentalSkyAngle();
        }

        playerYaw = Math.abs(playerYaw);

        // check if day,
        // and player is looking towards the sun, and that there isn't a block obstructing their view
        return skyAngle < 1
            && (playerYaw > 89 && playerYaw < 92 || (playerAngle > 0.45F && playerAngle < 0.55F))
            && playerAngle > (skyAngle - 0.04F) && playerAngle < (skyAngle + 0.04F)
            && entity.pick(100, 1, true).getType() == Type.MISS;
    }

    static float getSunIntensity(Level world) {
        float skyAngle = getSkyAngle(world);
        if (skyAngle > SUNSET) {
            return 0;
        }

        // intensity (0-1) has a peak at 0.5 (midday)
        float intensity = Mth.cos((skyAngle - 0.5F) * Mth.PI);

        if (world.isRaining()) {
            intensity *= 0.5;
        }
        if (world.isThundering()) {
            intensity *= 0.5;
        }

        return intensity;
    }

    /**
     * Gets the sun angle on a scale of 0-2 where 0=sunrise, 1=sunset, and >1 is night
     *
     * 0 = sunrise
     * 0-0.5 = morning
     * 0.5 = noon
     * 0.5-1 = evening
     * 1 = sunset
     * 1-1.5 = night
     * 1.5 = midnight
     * 1.5-0 = night
     *
     * @param world
     * @return The sun angle
     */
    static float getSkyAngle(Level world) {
        return (world.getOverworldClockTime() % 24000L) / 12000F;
    }

    /**
     * Gets the current day phase.
     */
    static DayPhase getDayPhase(Level world) {
        float skyAngle = getSkyAngle(world);

        if (isCloseTo(skyAngle, SUNRISE, 0.1F)) {
            return DayPhase.SUNRISE;
        }
        if (skyAngle < 0.5F) {
            return DayPhase.MORNING;
        }
        if (isCloseTo(skyAngle, NOON, 0.1F)) {
            return DayPhase.NOON;
        }
        if (skyAngle < 1F) {
            return DayPhase.EVENING;
        }
        if (isCloseTo(skyAngle, SUNSET, 0.1F)) {
            return DayPhase.SUNSET;
        }
        if (skyAngle < MIDNIGHT) {
            return DayPhase.NIGHT;
        }
        if (isCloseTo(skyAngle, MIDNIGHT, 0.1F)) {
            return DayPhase.MIDNIGHT;
        }
        return DayPhase.AFTER_MIDNIGHT;
    }

    static boolean isPositionExposedToSun(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            world.updateSkyBrightness();
        }

        return world.dimensionType().hasSkyLight()
                && world.getBrightness(LightLayer.SKY, pos) >= 12
                && !world.isRaining()
                && !world.isThundering()
                && world.isBrightOutside();
    }

    public enum DayPhase implements StringRepresentable {
        SUNRISE(true),
        MORNING(true),
        NOON(true),
        EVENING(true),
        SUNSET(false),
        NIGHT(false),
        MIDNIGHT(false),
        AFTER_MIDNIGHT(false);

        public static final DayPhase[] VALUES = values();

        public static final Codec<DayPhase> CODEC = StringRepresentable.fromEnum(DayPhase::values);
        public static final StreamCodec<ByteBuf, DayPhase> PACKET_CODEC = ByteBufCodecs.idMapper(i -> VALUES[i], DayPhase::ordinal);

        private final boolean day;
        private final boolean transitionary;

        private final String name = name().toLowerCase(Locale.ROOT);

        DayPhase(boolean day) {
            this.day = day;
            this.transitionary = ordinal() % 2 == 0;
        }

        public boolean isDay() {
            return day;
        }

        public boolean isTransition() {
            return transitionary;
        }

        public DayPhase simplified() {
            if (this == SUNRISE) {
                return MORNING;
            }
            if (this == NOON || this == SUNSET) {
                return EVENING;
            }
            if (this == MIDNIGHT || this == AFTER_MIDNIGHT) {
                return NIGHT;
            }
            return this;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
