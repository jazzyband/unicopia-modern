package com.minelittlepony.unicopia.server.world;

import java.util.Locale;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgZapAppleStage;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil.DayPhase;
import io.netty.buffer.ByteBuf;

public class ZapAppleStageStore extends SavedData {
    private static final Identifier ID = Unicopia.id("zap_apple_stage");
    static final long DAY_LENGTH = 24000L;
    static final long MOON_PHASES = DimensionType.MOON_BRIGHTNESS_PER_PHASE.length;
    private static final PersistentStateKey<ZapAppleStageStore> KEY = new PersistentStateKey<>(ID, RecordCodecBuilder.create(o -> o.group(
            Stage.CODEC.fieldOf("stage").forGetter(i -> i.lastStage),
            DayPhase.CODEC.optionalFieldOf("phase", null).forGetter(i -> i.lastPhase),
            Codec.BOOL.fieldOf("playedMoonEffect").forGetter(i -> i.playedMoonEffect),
            Codec.INT.fieldOf("nextLightningEvent").forGetter(i -> i.nextLightningEvent)
    ).apply(o, ZapAppleStageStore::new)), ZapAppleStageStore::new);

    public static ZapAppleStageStore get(LevelReader world) {
        return KEY.get(world);
    }

    private Stage lastStage = Stage.HIBERNATING;
    @Nullable
    private DayPhase lastPhase;

    private boolean playedMoonEffect;
    private int nextLightningEvent = 1200;

    private ZapAppleStageStore() { }

    private ZapAppleStageStore(Stage lastStage, @Nullable DayPhase lastPhase, boolean playedMoonEffect, int nextLightningEvent) {
        this.lastStage = lastStage;
        this.lastPhase = lastPhase;
        this.playedMoonEffect = playedMoonEffect;
        this.nextLightningEvent = nextLightningEvent;
    }

    public void tick(Level world) {
        DayPhase phase = MeteorlogicalUtil.getDayPhase(world);

        if (phase != lastPhase) {
            // only trigger on the day-to-night transition
            if (!phase.isDay() && (lastPhase == null || phase.isDay() != lastPhase.isDay())
                    // cycle starts on a full moon
                    && (lastStage != Stage.HIBERNATING || MeteorlogicalUtil.getMoonPhase(world) == MeteorlogicalUtil.FULL_MOON)) {
                progressStage(world);
            }

            lastPhase = phase;
            setDirty();
        }


        if (!lastPhase.isDay() && nextLightningEvent > 0) {
            nextLightningEvent--;
            setDirty();
        }
    }

    private void progressStage(Level world) {
        lastStage = lastStage.getNext();
        playedMoonEffect = false;
        Channel.SERVER_ZAP_STAGE.sendToAllPlayers(new MsgZapAppleStage(lastStage), world);
    }

    public void playMoonEffect(Level world, BlockPos pos) {
        if (!playedMoonEffect) {
            playedMoonEffect = true;
            setDirty();

            world.playSeededSound(null, pos.getX(), pos.getY(), pos.getZ(), USounds.AMBIENT_ZAP_APPLE_RIPEN, SoundSource.BLOCKS, 1.5F, 0.3F, world.getRandom().nextInt(1200));
        }
    }

    public void triggerLightningStrike(Level world, BlockPos pos) {
        world.gameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Context.of(world.getBlockState(pos)));
        ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, Vec3.atCenterOf(pos), Vec3.ZERO);

        if (nextLightningEvent <= 0) {
            StreamSupport.stream(BlockPos.randomInCube(world.getRandom(), 20, pos, 10).spliterator(), false)
                .filter(p -> world.isEmptyBlock(p) && !world.isEmptyBlock(p.below()) && world.canSeeSky(p))
                .findFirst().ifPresent(p -> {
                    LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(world, EntitySpawnReason.EVENT);
                    bolt.snapTo(Vec3.atBottomCenterOf(pos));
                    bolt.setVisualOnly(true);
                    world.addFreshEntity(bolt);
                    nextLightningEvent = world.getRandom().nextIntBetweenInclusive(1200, 8000);
                    setDirty();
                });
        }
    }

    /**
     * Returns the current zap apple ripening stage.
     */
    public Stage getStage() {
        return lastStage;
    }

    public enum Stage implements StringRepresentable {
        HIBERNATING,
        GREENING,
        FLOWERING,
        FRUITING,
        RIPE;

        static final Stage[] VALUES = values();
        static final float MAX = VALUES.length;

        public static final Codec<Stage> CODEC = StringRepresentable.fromEnum(Stage::values);
        public static final StreamCodec<ByteBuf, Stage> PACKET_CODEC = ByteBufCodecs.idMapper(i -> VALUES[i], Stage::ordinal);

        private final float ordinal = ordinal();

        public Stage getNext() {
            return byId((ordinal() + 1) % VALUES.length);
        }

        public Stage getPrevious() {
            return byId(((ordinal() - 1) + VALUES.length) % VALUES.length);
        }

        public float getStageProgress(@Nullable Level world) {
            if (world == null) {
                return 0;
            }
            float skyAngle = MeteorlogicalUtil.getSkyAngle(world);
            float dayProgress = ((skyAngle + 1.5F) % 3F) / 3F;
            if (this == HIBERNATING) {
                return (MeteorlogicalUtil.getMoonPhase(world) + dayProgress) / MOON_PHASES;
            }

            return dayProgress;
        }

        public float getCycleProgress(@Nullable Level world) {
            return Mth.lerp(getStageProgress(world), ordinal, ordinal + 1) / MAX;
        }

        private static Stage byId(int id) {
            return VALUES[Mth.clamp(id, 0, VALUES.length)];
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
