package com.minelittlepony.unicopia.network.track;

import net.minecraft.world.entity.Entity;

public interface Trackable {
    DataTrackerManager getDataTrackers();

    static Trackable of(Entity entity) {
        return (Trackable)entity;
    }
}
