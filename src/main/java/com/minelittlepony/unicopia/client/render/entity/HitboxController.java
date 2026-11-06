package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.util.Untyped;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;

public interface HitboxController<T extends Entity> {
    HitboxController<?> DEFAULT = e -> true;

    boolean shouldRenderHitbox(T entity);

    static <E extends Entity> HitboxController<E> of(EntityRenderer<E, ?> renderer) {
        return Untyped.cast(renderer instanceof HitboxController h ? h : DEFAULT);
    }
}
