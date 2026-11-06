package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.collision.EntityCollisions;

@Mixin(EntityGetter.class)
interface MixinEntityView {
    @ModifyReturnValue(
            method = "getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
            at = @At("RETURN")
    )
    private List<VoxelShape> onGetEntityCollisions(List<VoxelShape> original, @Nullable Entity entity, AABB box) {
        if (box.getSize() < 1.0E-7D) {
            return original;
        }

        List<VoxelShape> shapes = EntityCollisions.getColissonShapes(entity, (EntityGetter)this, box);
        if (!shapes.isEmpty()) {
            return Stream.concat(shapes.stream(), original.stream()).toList();
        }
        return original;
    }
}
