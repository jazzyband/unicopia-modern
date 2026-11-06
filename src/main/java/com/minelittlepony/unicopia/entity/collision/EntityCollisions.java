package com.minelittlepony.unicopia.entity.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;

public class EntityCollisions {

    public static void getCollissionShapes(@Nullable Entity entity, CollisionContext context, Consumer<VoxelShape> output) {
        if (entity == null) {
            return;
        }

        if (entity.canBeCollidedWith(context instanceof EntityCollisionContext collision ? collision.getEntity() : null)) {
            output.accept(Shapes.create(entity.getBoundingBox()));
            if (entity instanceof ComplexCollidable collidable) {
                collidable.getCollissionShapes(context, output);
            }
        } else if (entity instanceof FallingBlockEntity) {
            BlockPos pos = entity.blockPosition();
            output.accept(((FallingBlockEntity) entity).getBlockState()
                    .getCollisionShape(entity.level(), entity.blockPosition(), context)
                    .move(pos.getX(), pos.getY(), pos.getZ())
            );
        }
    }

    public static List<VoxelShape> getColissonShapes(@Nullable Entity entity, EntityGetter world, AABB box) {
        CollisionContext ctx = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
        return collectCollisionBoxes(box, collector -> {
            world.getEntities(entity, box.inflate(50), e -> {
                Caster.of(e).flatMap(c -> c.getSpellSlot().get(SpellPredicate.IS_DISGUISE)).ifPresent(p -> {
                    p.getAppearance().getCollissionShapes(ctx, collector);
                });
                if (e instanceof ComplexCollidable collidable) {
                    collidable.getCollissionShapes(ctx, collector);
                }
                return false;
            });
        });
    }

    static List<VoxelShape> collectCollisionBoxes(AABB box, Consumer<Consumer<VoxelShape>> generator) {
        List<VoxelShape> shapes = new ArrayList<>();
        VoxelShape entityShape = Shapes.create(box.inflate(1.0E-6D));
        generator.accept(shape -> {
            if (!shape.isEmpty() && Shapes.joinIsNotEmpty(shape, entityShape, BooleanOp.AND)) {
                shapes.add(shape);
            }
        });
        return shapes;
    }

    public interface ComplexCollidable {
        void getCollissionShapes(CollisionContext context, Consumer<VoxelShape> output);
    }
}
