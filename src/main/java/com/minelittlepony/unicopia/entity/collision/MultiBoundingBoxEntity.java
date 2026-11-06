package com.minelittlepony.unicopia.entity.collision;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.minelittlepony.unicopia.entity.collision.EntityCollisions.ComplexCollidable;

public interface MultiBoundingBoxEntity extends ComplexCollidable {
    List<AABB> getBoundingBoxes(AABB mainBox);

    default List<AABB> getGravityZoneBoxes() {
        return getBoundingBoxes(((Entity)this).getBoundingBox());
    }

    Map<AABB, List<Entity>> getCollidingEntities(Stream<AABB> boundingBoxes);

    SoundEvent getWalkedOnSound(double y);

    @Override
    default void getCollissionShapes(CollisionContext context, Consumer<VoxelShape> output) {
        for (AABB box : getBoundingBoxes(((Entity)this).getBoundingBox())) {
            output.accept(Shapes.create(box));
        }
    }

    static List<AABB> getBoundingBoxes(Entity entity) {
        return entity instanceof MultiBoundingBoxEntity multi ? multi.getBoundingBoxes(entity.getBoundingBox()) : List.of(entity.getBoundingBox());
    }
}
