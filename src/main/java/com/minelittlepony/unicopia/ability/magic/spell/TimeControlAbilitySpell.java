package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/**
 * Internal.
 * <p>
 * Used by the time change ability
 */
public class TimeControlAbilitySpell extends AbstractSpell {

    private boolean initilized;

    private long timeOffset;
    private float angleOffset;

    public TimeControlAbilitySpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (!source.asWorld().getServer().getGameRules().get(UGameRules.DO_TIME_MAGIC) || situation != Situation.BODY || !source.canUse(Abilities.TIME)) {
            return false;
        }

        update(source, Rot.of(source));
        return source.subtractEnergyCost(2);
    }

    public void update(Caster<?> source, Rot rotation) {
        if (!source.asWorld().getServer().getGameRules().get(UGameRules.DO_TIME_MAGIC)) {
            return;
        }
        if (!(source.asWorld() instanceof ServerLevel sw)) {
            return;
        }

        float yaw = -(rotation.yaw() + 90);
        float pitch = -(rotation.pitch() / 90F);
        long time = (long)(pitch * 6000);

        // sunrise(0) - midday(1) - sunset(2) - midnight(3)

        if (!initilized) {
            initilized = true;
            timeOffset = sw.getOverworldClockTime() - time;
            angleOffset = UnicopiaWorldProperties.forWorld(sw).getTangentalSkyAngle() - yaw;
        }

        if (angleOffset > 90 && angleOffset < 270) {
            time *= -1;
        }

        time += timeOffset;
        if (time < 0) {
            time += 24000;
        }
        time %= 24000;

        sw.clockManager().setTotalTicks(sw.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.WORLD_CLOCK)
                .getOrThrow(net.minecraft.world.clock.WorldClocks.OVERWORLD), time);

        UnicopiaWorldProperties.forWorld(sw).setTangentalSkyAngle(angleOffset + yaw);
    }

    @Override
    public void toNBT(CompoundTag compound, Provider lookup) {
        super.toNBT(compound, lookup);
        compound.putBoolean("initilized", initilized);
        compound.putLong("timeOffset", timeOffset);
        compound.putFloat("angleOffset", angleOffset);
    }

    @Override
    public void fromNBT(CompoundTag compound, Provider lookup) {
        super.fromNBT(compound, lookup);
        initilized = compound.getBooleanOr("initilized", false);
        timeOffset = compound.getLongOr("timeOffset", 0);
        angleOffset = compound.getFloatOr("angleOffset", 0);
    }
}
