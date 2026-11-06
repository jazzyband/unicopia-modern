package com.minelittlepony.unicopia.entity.mob;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface ArenaCombatant {
    Optional<BlockPos> getHomePos();

    default float getAreaRadius() {
        return 30;
    }

    boolean teleportTo(Vec3 destination);
}
