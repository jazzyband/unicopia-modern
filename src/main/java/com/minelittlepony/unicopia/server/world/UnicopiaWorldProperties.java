package com.minelittlepony.unicopia.server.world;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSkyAngle;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class UnicopiaWorldProperties extends SavedData {
    static final Codec<Set<BlockPos>> POS_CODEC = CodecUtils.setOf(BlockPos.CODEC);
    private static final PersistentStateKey<UnicopiaWorldProperties> KEY = new PersistentStateKey<>(
            Unicopia.id("tribes"),
            world -> RecordCodecBuilder.create(i -> i.group(
                    Race.CODEC.fieldOf("defaultRace").forGetter(o -> o.defaultRace),
                    Codec.FLOAT.fieldOf("tangentalSkyAngle").forGetter(o -> o.tangentalSkyAngle),
                    POS_CODEC.fieldOf("activeAltars").forGetter(o -> o.activeAltarPositions)
            ).apply(i, (defaultRace, tangentalSkyAngle, activeAltars) -> new UnicopiaWorldProperties((ServerLevel)world, defaultRace, tangentalSkyAngle, activeAltars))),
            world -> new UnicopiaWorldProperties((ServerLevel)world)
    );

    private final ServerLevel world;

    private Race defaultRace = Race.UNSET;
    private float tangentalSkyAngle;

    private final Set<BlockPos> activeAltarPositions = new HashSet<>();

    public static UnicopiaWorldProperties forWorld(ServerLevel world) {
        return KEY.get(world);
    }

    private UnicopiaWorldProperties(ServerLevel world) {
        this.world = world;
    }

    private UnicopiaWorldProperties(ServerLevel world, Race defaultRace, float tangentalSkyAngle, Set<BlockPos> activeAltars) {
        this(world);
        this.defaultRace = defaultRace;
        this.tangentalSkyAngle = tangentalSkyAngle;
        this.activeAltarPositions.addAll(activeAltars);
    }

    public Race getDefaultRace() {
        return defaultRace;
    }

    public Race setDefaultRace(Race race) {
        defaultRace = race;
        setDirty();
        return defaultRace;
    }

    public float getTangentalSkyAngle() {
        return tangentalSkyAngle;
    }

    public void setTangentalSkyAngle(float angle) {
        tangentalSkyAngle = Mth.wrapDegrees(angle);
        setDirty();
        Channel.SERVER_SKY_ANGLE.sendToAllPlayers(new MsgSkyAngle(tangentalSkyAngle), world);
    }

    public void removeAltar(BlockPos center) {
        activeAltarPositions.remove(center);
        setDirty();
    }

    public void addAltar(BlockPos center) {
        activeAltarPositions.add(center);
        setDirty();
    }

    public boolean isActiveAltar(BlockPos center) {
        return activeAltarPositions.contains(center);
    }

    public boolean isActiveAltar(Entity entity) {
        for (int i = 0; i < entity.getBbHeight(); i++) {
            if (isActiveAltar(entity.blockPosition().above(i))) {
                return true;
            }
        }
        return false;
    }
}
