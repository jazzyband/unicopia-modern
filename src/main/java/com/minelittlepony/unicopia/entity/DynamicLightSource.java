package com.minelittlepony.unicopia.entity;

import java.lang.ref.WeakReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import com.minelittlepony.unicopia.server.world.LightSources;

public interface DynamicLightSource {
    default int getLightLevel() {
        return 0;
    }

    default BlockPos getLightSourcePosition() {
        return ((Entity)this).blockPosition();
    }

    static final class LightEmitter<T extends Entity & DynamicLightSource> {
        private final T entity;
        private WeakReference<Level> lastWorld = new WeakReference<>(null);

        public LightEmitter(T entity) {
            this.entity = entity;
        }

        public void tick() {
            if (entity.isRemoved() || entity.getLightLevel() <= 0) {
                remove();
                return;
            }

            Level world = entity.level();
            if (!lastWorld.refersTo(world)) {
                remove();
                lastWorld = new WeakReference<>(world);
            }

            LightSources.get(world).addLightSource(entity);
        }

        public void remove() {
            Level world = lastWorld.get();
            if (world != null) {
                LightSources.get(world).removeLightSource(entity.getUUID());
                lastWorld.clear();
            }
        }
    }
}