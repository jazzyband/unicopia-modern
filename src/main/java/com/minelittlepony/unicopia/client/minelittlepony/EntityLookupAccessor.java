package com.minelittlepony.unicopia.client.minelittlepony;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public interface EntityLookupAccessor {
    LevelEntityGetter<Entity> callGetEntityLookup();
}
