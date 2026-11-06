package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityTypes;

public class AnimalPoser {
    public static final AnimalPoser INSTANCE = new AnimalPoser();

    public void applyPosing(PoseStack matrices, EntityRenderState entity, EntityModel<?> model) {
        if (!(entity instanceof CasterState.Container container)) {
            return;
        }
        CasterState state = container.getUnicopiaState();

        if (state.type == EntityTypes.PIG) {
            java.util.Optional.ofNullable(model.root().createPartLookup().apply(PartNames.HEAD)).ifPresent(part -> {
                part.y = 12;
                part.xRot = state.eatingHeadAngle;
            });
        }
    }
}
