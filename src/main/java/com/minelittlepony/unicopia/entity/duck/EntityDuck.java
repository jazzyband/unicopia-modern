package com.minelittlepony.unicopia.entity.duck;

import java.util.Set;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.material.Fluid;
import com.minelittlepony.unicopia.compat.pehkui.PehkuiEntityExtensions;
import com.minelittlepony.unicopia.entity.behaviour.Guest;

public interface EntityDuck extends LavaAffine, PehkuiEntityExtensions, Guest {


    void setRemovalReason(RemovalReason reason);

    void setVehicle(Entity vehicle);

    float getNextStepSoundDistance();

    @Override
    default void setLavaAffine(boolean lavaAffine) {

    }
}
