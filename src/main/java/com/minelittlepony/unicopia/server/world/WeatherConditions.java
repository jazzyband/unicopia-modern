package com.minelittlepony.unicopia.server.world;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.StormCloudEntity;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

public class WeatherConditions extends SavedData {
    public static final double FIRE_UPDRAFT = 0.13;
    public static final double SAND_UPDRAFT = 0.03;
    public static final double SOUL_SAND_UPDRAFT = -0.03;
    public static final double ICE_UPDRAFT = 0;
    public static final double VOID_UPDRAFT = -0.23;

    public static final float MAX_UPDRAFT_HEIGHT = 20;
    public static final float MAX_TERRAIN_HEIGHT = 50;
    public static final float MAX_WIND_HEIGHT = 70;

    @SuppressWarnings("deprecation")
    public static final Plane THERMAL_FIELD = (world, pos) -> {
        if (!world.hasChunkAt(pos)) {
            return 0;
        }
        double factor = 1 - getScaledDistanceFromTerrain(pos, world, MAX_UPDRAFT_HEIGHT);
        return (float)(factor * getMaterialSurfaceTemperature(pos, world));
    };
    public static final Plane LOCAL_ALTITUDE_FIELD = (world, pos) -> {
        if (!world.isEmptyBlock(pos)) {
            return 0;
        }
        int y = pos.getY();
        do {
            pos.move(Direction.DOWN);
        } while (world.isEmptyBlock(pos) && world.isInWorldBounds(pos));
        return y - pos.getY();
    };

    private static final Identifier ID = Unicopia.id("weather_conditions");
    private static final PersistentStateKey<WeatherConditions> KEY = new PersistentStateKey<>(ID, RecordCodecBuilder.create(o -> o.group(
            Codec.FLOAT.fieldOf("windYaw").forGetter(i -> i.windYaw),
            Codec.FLOAT.fieldOf("prevWindYaw").forGetter(i -> i.prevWindYaw),
            Codec.BOOL.fieldOf("prevDayState").forGetter(i -> i.prevDayState),
            Codec.INT.fieldOf("interpolation").forGetter(i -> i.interpolation),
            Codec.INT.fieldOf("maxInterpolation").forGetter(i -> i.maxInterpolation)
    ).apply(o, WeatherConditions::new)), WeatherConditions::new);

    public static WeatherConditions get(LevelReader world) {
        return KEY.get(world);
    }

    private float windYaw;
    private float prevWindYaw;
    private int interpolation;
    private int maxInterpolation = 100;

    private boolean prevDayState;

    private Map<UUID, Storm> storms = new HashMap<>();

    private WeatherConditions() {
    }

    private WeatherConditions(float windYaw, float prevWindYaw, boolean prevDayState, int interpolation, int maxInterpolation) {
        this.windYaw = windYaw;
        this.prevWindYaw = prevWindYaw;
        this.prevDayState = prevDayState;
        this.interpolation = interpolation;
        this.maxInterpolation = maxInterpolation;
    }

    public void addStorm(StormCloudEntity cloud) {
        synchronized (storms) {
            storms.computeIfAbsent(cloud.getUUID(), id -> new Storm(cloud));
        }
    }

    public boolean isInRangeOfStorm(BlockPos pos) {
        synchronized (storms) {
            storms.values().removeIf(Storm::shouldRemove);
            return storms.values().stream().anyMatch(storm -> storm.inRange(pos));
        }
    }

    public void tick(Level world) {
        if (interpolation < maxInterpolation) {
            interpolation++;
            setDirty();
        }

        boolean isDay = world.isBrightOutside();
        if (isDay != prevDayState
            || world.getRandom().nextInt(1200) == 0
            || (world.isRaining() && world.getRandom().nextInt(120) == 0)
            || (world.isThundering() && world.getRandom().nextInt(90) == 0)) {
            prevDayState = isDay;
            prevWindYaw = getWindYaw();
            windYaw = world.getRandom().nextFloat() * 360;
            interpolation = 0;
            maxInterpolation = world.isRaining() || world.isThundering() ? 50 : 100;
            setDirty();
        }
    }

    public float getWindYaw() {
        return Mth.lerp(interpolation / (float)maxInterpolation, prevWindYaw, windYaw);
    }

    public int getWindInterpolation() {
        return interpolation;
    }

    public Vec3 getWindDirection() {
        return Vec3.directionFromRotation(0, getWindYaw()).normalize();
    }

    private class Storm {
        private final WeakReference<StormCloudEntity> cloud;

        public Storm(StormCloudEntity cloud) {
            this.cloud = new WeakReference<>(cloud);
        }

        public boolean inRange(BlockPos pos) {
            final StormCloudEntity cloud = this.cloud.get();
            if (cloud == null) {
                return false;
            }
            BlockPos cloudPos = cloud.blockPosition();
            if (pos.getY() > cloudPos.getY() + cloud.getBbHeight()) {
                return false;
            }
            float radius = cloud.getSizeInBlocks();
            return (cloudPos.getX() - pos.getX()) <= radius
                    && (cloudPos.getZ() - pos.getZ()) <= radius;
        }

        public boolean shouldRemove() {
            final StormCloudEntity cloud = this.cloud.get();
            return cloud == null || cloud.isRemoved() || !cloud.isStormy();
        }
    }

    @SuppressWarnings("deprecation")
    public static Vec3 getAirflow(BlockPos pos, Level world) {
        if (!world.hasChunkAt(pos)) {
            return Vec3.ZERO;
        }

        BlockPos.MutableBlockPos probedPosition = new BlockPos.MutableBlockPos();

        final float terrainFactor = 1 - getScaledDistanceFromTerrain(probedPosition.set(pos), world, MAX_TERRAIN_HEIGHT);
        final float windFactor = getScaledDistanceFromTerrain(probedPosition.set(pos), world, MAX_WIND_HEIGHT);

        Vec3 terrainGradient = LOCAL_ALTITUDE_FIELD.computeAverage(world, pos, probedPosition).scale(terrainFactor);
        Vec3 thermalGradient = THERMAL_FIELD.computeAverage(world, pos, probedPosition).scale(terrainFactor);
        Vec3 wind = get(world).getWindDirection().scale(windFactor);

        return terrainGradient
                .add(thermalGradient)
                .add(wind)
                .normalize()
                .scale(windFactor);
    }

    private static float getScaledDistanceFromTerrain(BlockPos.MutableBlockPos pos, Level world, float maxDistance) {
        return Math.min(maxDistance, LOCAL_ALTITUDE_FIELD.getValue(world, pos)) / maxDistance;
    }

    private static double getMaterialSurfaceTemperature(BlockPos.MutableBlockPos pos, Level world) {
        BlockState state = world.getBlockState(pos);

        if (state.isAir()) {
            return VOID_UPDRAFT;
        }

        if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
            return SOUL_SAND_UPDRAFT;
        }

        if (state.is(Blocks.LAVA) || state.is(Blocks.LAVA_CAULDRON)
                || state.is(BlockTags.FIRE)
                || state.is(BlockTags.CAMPFIRES)
                || state.is(Blocks.MAGMA_BLOCK)) {
            return FIRE_UPDRAFT;
        }

        if (state.is(BlockTags.SAND)) {
            return SAND_UPDRAFT * MeteorlogicalUtil.getSunIntensity(world);
        }

        if (state.is(BlockTags.SNOW) || state.is(BlockTags.ICE)) {
            return ICE_UPDRAFT * MeteorlogicalUtil.getSunIntensity(world);
        }

        if (state.getFluidState().is(FluidTags.WATER)) {
            float sunIntensity = MeteorlogicalUtil.getSunIntensity(world);
            int depth = 0;
            BlockPos.MutableBlockPos mutable = pos.mutable();
            while (depth < 15 && world.getFluidState(mutable).is(FluidTags.WATER)) {
                mutable.move(Direction.DOWN);
                depth++;
            }

            return sunIntensity * (depth / 15F);
        }

        return 0;
    }

    public static Vec3 getGustStrength(Level world, BlockPos pos) {
        RandomSource random = getPositionalRandom(world, pos);
        float strength = 0.015F * random.nextFloat();

        if (random.nextInt(30) == 0) {
            strength *= 10;
        }
        if (random.nextInt(30) == 0) {
            strength *= 10;
        }
        if (random.nextInt(40) == 0) {
            strength *= 100;
        }

        strength = Math.min(strength, 7);

        float pitch = (180 * random.nextFloat()) - 90;
        float yaw = (180 * random.nextFloat()) - 90;

        return new Vec3(strength * world.getRainLevel(1), pitch, yaw);
    }

    public static RandomSource getPositionalRandom(Level world, BlockPos pos) {
        long posLong = ChunkPos.pack(pos);
        long time = world.getGameTime();
        return RandomSource.create(posLong + time);
    }

    public interface Plane {
        float getValue(Level world, BlockPos.MutableBlockPos pos);

        default Vec3 computeAverage(Level world, BlockPos pos, BlockPos.MutableBlockPos probedPosition) {

            float e = getValue(world, probedPosition.set(pos));

            // A(-1,-1) B( 0,-1) C( 1,-1)
            // D(-1, 0) E( 0, 0) F( 1, 0)
            // G(-1, 1) H( 0, 1) I( 1, 1)

            int projectionDistance = 4;

            // DEF
            Vec3 def = new Vec3(-average(
                    getValue(world, probedPosition.set(pos).move(Direction.WEST, projectionDistance)) - e,
                    e - getValue(world, probedPosition.set(pos).move(Direction.EAST, projectionDistance))
            ), 0, 0).normalize();
            // BEH
            Vec3 beh = new Vec3(0, 0, -average(
                    getValue(world, probedPosition.set(pos).move(Direction.NORTH, projectionDistance)) - e,
                    e - getValue(world, probedPosition.set(pos).move(Direction.SOUTH, projectionDistance))
            )).normalize();
            // AEI
            Vec3 aei = diag(average(
                    getValue(world, probedPosition.set(pos).move(Direction.WEST, projectionDistance).move(Direction.NORTH, projectionDistance)) - e,
                    e - getValue(world, probedPosition.set(pos).move(Direction.EAST, projectionDistance).move(Direction.SOUTH, projectionDistance))
            )).normalize();
            // GEC
            Vec3 gec = diag(average(
                    getValue(world, probedPosition.set(pos).move(Direction.EAST, projectionDistance).move(Direction.NORTH, projectionDistance)) - e,
                    e - getValue(world, probedPosition.set(pos).move(Direction.EAST, projectionDistance).move(Direction.WEST, projectionDistance))
            )).normalize();

            return beh.add(def).add(aei).add(gec).normalize();
        }

        private static Vec3 diag(double mag) {
            return new Vec3(0.5 * mag, 0, 0.5 * mag);
        }

        private static float average(float a, float b) {
            return (a + b) / 2F;
        }
    }
}
